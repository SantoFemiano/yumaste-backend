package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.CheckoutRequestDTO;
import com.yumaste.yumasteapi.DTO.response.CartDTO;
import com.yumaste.yumasteapi.DTO.response.CartItemDTO;
import com.yumaste.yumasteapi.DTO.response.OrdineResponseDTO;
import com.yumaste.yumasteapi.DTO.response.OrdiniDettagliDTO;
import com.yumaste.yumasteapi.events.OrderCreatedEvent;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.exceptions.UnauthorizedException;
import com.yumaste.yumasteapi.mapper.OrderDettagliMapper;
import com.yumaste.yumasteapi.models.*;
import com.yumaste.yumasteapi.repositories.*;
import com.yumaste.yumasteapi.services.email.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final BoxRepository boxRepository;

    private final OrdineRepository ordineRepository;
    private final DettaglioOrdineRepository dettaglioOrdineRepository;
    private final SpedizioneRepository spedizioneRepository;
    private final FatturaRepository fatturaRepository;
    private final IndirizzoUtenteRepository indirizzoRepository;
    private final OrderDettagliMapper orderDettagliMapper;
    private final EmailService emailService;

    private final org.springframework.kafka.core.KafkaTemplate<String,Object> kafkaTemplate;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ordini", key = "#utente.id"),
            @CacheEvict(value = "ordini", allEntries = true)
    })
    public OrdineResponseDTO checkout(Utente utente, CheckoutRequestDTO requestDTO){
        //recupero carrello dell utente corrente
        CartDTO carrello = cartService.getCarrelloDellUtente(utente);

        //controllo se carrello è vuoto
        if(carrello.items().isEmpty()){
            throw new BusinessException("Impossibile effettuare l'ordine: il carrello è vuoto.");
        }

        //recupero indirizzo utente da id_utente richiesta e controllo
        IndirizzoUtente indirizzo = indirizzoRepository.findById(requestDTO.indirizzoId()).filter(
                        ind -> ind.getUtente().getId().equals(utente.getId()))
                .orElseThrow(()-> new ResourceNotFoundException("Indirizzo non trovato o non appartenente all'utente")
                );

        //creazione oggetto ordine e compilazione oggetto
        Ordine ordine = new Ordine();
        ordine.setUtente(utente);
        ordine.setCodiceOrdine("ORD-"+ UUID.randomUUID().toString().substring(0,8).toUpperCase());
        ordine.setDataOrdine(Instant.now());
        ordine.setTotalePrezzo(carrello.totalPrice());
        ordine.setTotaleQuantita(carrello.totalQuantity());
        ordine.setStatoOrdine("IN_ATTESA");

        Ordine ordinesalvato = ordineRepository.save(ordine);






        List<Long> boxIds = carrello.items().stream().map(CartItemDTO::boxId).toList();

        java.util.Map<Long, Box> boxMap = boxRepository.findAllById(boxIds).stream()
                .collect(java.util.stream.Collectors.toMap(Box::getId, b -> b));

        List<DettaglioOrdine> dettagliDaSalvare = new java.util.ArrayList<>();

        // 3. Cicliamo gli item del carrello
        for(CartItemDTO item : carrello.items()){
            DettaglioOrdine dettaglio = new DettaglioOrdine();
            dettaglio.setOrdine(ordinesalvato);

            // Peschiamo la box dalla mappa in memoria (ZERO chiamate al DB in questo ciclo!)
            Box box = boxMap.get(item.boxId());
            if(box == null) {
                throw new ResourceNotFoundException("Box non trovata!");
            }

            dettaglio.setBox(box);
            dettaglio.setQuantita(item.quantita());
            dettaglio.setPrezzoUnitario(item.prezzoScontato());

            // Aggiungiamo l'entità alla lista invece di salvarla subito
            dettagliDaSalvare.add(dettaglio);
        }

        // 4. Eseguiamo una SINGOLA operazione di INSERT multipla nel database! (Velocissimo)
        dettaglioOrdineRepository.saveAll(dettagliDaSalvare);

        // =====================================================================

        // creo oggetto spedizione associato all ordine
        Spedizione spedizione = new Spedizione();
        spedizione.setOrdine(ordinesalvato);
        spedizione.setStatoSpedizione("IN_PREPARAZIONE");
        spedizione.setCorriere("DA_ASSEGNARE");

        spedizione.setVia(indirizzo.getVia());
        spedizione.setCivico(indirizzo.getCivico());
        spedizione.setCap(indirizzo.getCap());
        spedizione.setCitta(indirizzo.getCitta());
        spedizione.setProvincia(indirizzo.getProvincia());

        Spedizione spedizionesalvata = spedizioneRepository.save(spedizione);

        //creo oggetto fattura associato all ordine
        Fattura fattura = new Fattura();
        fattura.setOrdine(ordinesalvato);
        fattura.setMetodoPagamento(requestDTO.metodoPagamento());
        fattura.setImporto(carrello.totalPrice());
        fattura.setDataPagamento(LocalDate.now());

        fatturaRepository.save(fattura);

        OrderCreatedEvent evento = new OrderCreatedEvent(
                ordinesalvato.getId(),
                utente.getEmail(),
                utente.getNome()
        );

        kafkaTemplate.send("order-created-topic", evento);

        cartRepository.deleteAll(cartRepository.findByUtente(utente));

        //ritorno del dto ordine compilato
        return new OrdineResponseDTO(
                ordinesalvato.getId(),
                ordinesalvato.getCodiceOrdine(),
                ordinesalvato.getDataOrdine(),
                ordinesalvato.getTotalePrezzo(),
                ordinesalvato.getStatoOrdine(),
                spedizionesalvata.getStatoSpedizione(),
                utente.getId(),
                utente.getNome(),
                utente.getCognome()
        );
    }

    @Cacheable(value="ordini",key = "#utente.id")
    public List<OrdineResponseDTO> findAllOrdini(Utente utente) {
        return ordineRepository.findByUtente(utente)
                .stream()
                .map(this::creaOrdineDTO) // Usiamo il nostro metodo!
                .toList();
    }

    @Cacheable(value = "ordini")
    public List<OrdineResponseDTO> findAllOrdini() {
        return ordineRepository.findAll()
                .stream()
                .map(this::creaOrdineDTO) // Usiamo il nostro metodo!
                .toList();
    }

    @Cacheable(value = "dettaglio_ordine_admin", key="#idOrdine")
    public List<OrdiniDettagliDTO> getDettagliOrdineAdmin(Long idOrdine) {
        Ordine ordine = ordineRepository.findById(idOrdine)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato!"));
        Spedizione spedizione = spedizioneRepository.findByOrdine(ordine)
                .orElseThrow(() -> new ResourceNotFoundException("Spedizione non trovata!"));
        Fattura fattura = fatturaRepository.findByOrdine(ordine)
                .orElseThrow(() -> new ResourceNotFoundException("Fattura non trovata!"));

        List<DettaglioOrdine> dettaglioOrdine = dettaglioOrdineRepository.findByOrdine_Id(idOrdine);

        return dettaglioOrdine.stream()
                .map(singoloDettaglio -> orderDettagliMapper.toDto(ordine, singoloDettaglio, fattura, spedizione))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "dettaglio_ordine", key="#idOrdine")
    public List<OrdiniDettagliDTO> getDettagliOrdini(Utente utenteCorrente, Long idOrdine){

        Ordine ordine = ordineRepository.findById(idOrdine).orElseThrow(() -> new ResourceNotFoundException("Ordine non trovata!"));

        if (!ordine.getUtente().getId().equals(utenteCorrente.getId())) {
            // Se non coincidono, blocchiamo tutto! L'utente sta provando a spiare un ordine altrui.
            throw new UnauthorizedException("Accesso negato: non sei autorizzato a visualizzare questo ordine.");
        }

        Spedizione spedizione = spedizioneRepository.findByOrdine(ordine).orElseThrow(() -> new ResourceNotFoundException("Spedizione non trovata!"));

            Fattura fattura = fatturaRepository.findByOrdine(ordine).orElseThrow(() -> new ResourceNotFoundException("Fattura non trovata!"));
        List<DettaglioOrdine> dettaglioOrdine = dettaglioOrdineRepository.findByOrdine_Id(idOrdine);

        return dettaglioOrdine.stream()
                .map(singoloDettaglio -> orderDettagliMapper.toDto(ordine, singoloDettaglio, fattura, spedizione))
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ordini", allEntries = true), // Svuota le liste (utente e admin)
            @CacheEvict(value = "dettaglio_ordine", key = "#id"),
            @CacheEvict(value = "dettaglio_ordine_admin", key = "#id")
    })
    public OrdineResponseDTO updateStatoOrdine(Long id, String statoOrdine, String statoSpedizione) {
        Ordine ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato"));

        if (statoOrdine != null && !statoOrdine.isBlank()) {
            ordine.setStatoOrdine(statoOrdine);
        }

        if (statoSpedizione != null && !statoSpedizione.isBlank()) {
            Spedizione spedizione = spedizioneRepository.findByOrdine(ordine)
                    .orElseThrow(() -> new ResourceNotFoundException("Spedizione non trovata"));
            spedizione.setStatoSpedizione(statoSpedizione);
            spedizioneRepository.save(spedizione);
        }

        ordineRepository.save(ordine);

        return creaOrdineDTO(ordine);
    }

    private OrdineResponseDTO creaOrdineDTO(Ordine ordine) {
        String statoSped = "IN_ATTESA";

        //spedizione collegata a questo ordine
        Optional<Spedizione> optSpedizione = spedizioneRepository.findByOrdine(ordine);
        if (optSpedizione.isPresent()) {
            statoSped = optSpedizione.get().getStatoSpedizione();
        }

        return new OrdineResponseDTO(
                ordine.getId(),
                ordine.getCodiceOrdine(),
                ordine.getDataOrdine(),
                ordine.getTotalePrezzo(),
                ordine.getStatoOrdine(),
                statoSped,
                ordine.getUtente().getId(),
                ordine.getUtente().getNome(),
                ordine.getUtente().getCognome()
        );
    }

}
