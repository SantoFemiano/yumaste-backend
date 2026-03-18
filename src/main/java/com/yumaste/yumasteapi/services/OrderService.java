package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.CheckoutRequestDTO;
import com.yumaste.yumasteapi.DTO.response.CartDTO;
import com.yumaste.yumasteapi.DTO.response.CartItemDTO;
import com.yumaste.yumasteapi.DTO.response.OrdineResponseDTO;
import com.yumaste.yumasteapi.DTO.response.OrdiniDettagliDTO;
import com.yumaste.yumasteapi.mapper.OrderDettagliMapper;
import com.yumaste.yumasteapi.mapper.OrderMapper;
import com.yumaste.yumasteapi.models.*;
import com.yumaste.yumasteapi.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final OrderMapper orderMapper;
    private final OrderDettagliMapper orderDettagliMapper;


    @Transactional
    public OrdineResponseDTO checkout(Utente utente, CheckoutRequestDTO requestDTO){
        //recupero carrello dell utente corrente
        CartDTO carrello = cartService.getCarrelloDellUtente(utente);

        //controllo se carrello è vuoto
        if(carrello.items().isEmpty()){
            throw  new RuntimeException("Impossibile effettuare l'ordine: il carrello è vuoto.");
        }

        //recupero indirizzo utente da id_utente richiesta e controllo
        IndirizzoUtente indirizzo= indirizzoRepository.findById(requestDTO.indirizzoId()).filter(
                ind -> ind.getUtente().getId().equals(utente.getId()))
                .orElseThrow(()-> new RuntimeException("Indirizzo non trovato o non appartenente all'utente")
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

//ciclo tutti gli item del carrello e creo per ogni item riga in dettaglio ordine
        for(CartItemDTO item : carrello.items()){
            DettaglioOrdine dettaglio = new DettaglioOrdine();
            dettaglio.setOrdine(ordinesalvato);

            Box box = boxRepository.findById(item.boxId()).orElseThrow(() -> new RuntimeException("Box non trovata!") );
            dettaglio.setBox(box);
            dettaglio.setQuantita(item.quantita());
            dettaglio.setPrezzoUnitario(item.prezzoScontato());
            dettaglioOrdineRepository.save(dettaglio);
        }

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

    public List<OrdineResponseDTO> findAllOrdini(Utente utente) {
        return ordineRepository.findByUtente(utente)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public List<OrdineResponseDTO> findAllOrdini() {
        return ordineRepository.findAll()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public List<OrdiniDettagliDTO> getDettagliOrdineAdmin(Long idOrdine) {
        Ordine ordine = ordineRepository.findById(idOrdine)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato!"));
        Spedizione spedizione = spedizioneRepository.findByOrdine(ordine)
                .orElseThrow(() -> new RuntimeException("Spedizione non trovata!"));
        Fattura fattura = fatturaRepository.findByOrdine(ordine)
                .orElseThrow(() -> new RuntimeException("Fattura non trovata!"));

        List<DettaglioOrdine> dettaglioOrdine = dettaglioOrdineRepository.findByOrdine_Id(idOrdine);

        return dettaglioOrdine.stream()
                .map(singoloDettaglio -> orderDettagliMapper.toDto(ordine, singoloDettaglio, fattura, spedizione))
                .collect(Collectors.toList());
    }


    public List<OrdiniDettagliDTO> getDettagliOrdini(Utente utenteCorrente, Long idOrdine){

        Ordine ordine = ordineRepository.findById(idOrdine).orElseThrow(() -> new RuntimeException("Ordine non trovata!"));

        if (!ordine.getUtente().getId().equals(utenteCorrente.getId())) {
            // Se non coincidono, blocchiamo tutto! L'utente sta provando a spiare un ordine altrui.
            throw new RuntimeException("Accesso negato: non sei autorizzato a visualizzare questo ordine.");
        }

        Spedizione spedizione = spedizioneRepository.findByOrdine(ordine).orElseThrow(() -> new RuntimeException("Spedizione non trovata!"));

        Fattura fattura = fatturaRepository.findByOrdine(ordine).orElseThrow(() -> new RuntimeException("Spedizione non trovata!"));
        List<DettaglioOrdine> dettaglioOrdine = dettaglioOrdineRepository.findByOrdine_Id(idOrdine);

        return dettaglioOrdine.stream()
                .map(singoloDettaglio -> orderDettagliMapper.toDto(ordine, singoloDettaglio, fattura, spedizione))
                .collect(Collectors.toList());
    }



}
