package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.AggiornaQuantitaDTO;
import com.yumaste.yumasteapi.DTO.response.CartItemDTO;
import com.yumaste.yumasteapi.DTO.response.CartDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Carrello;
import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.CartRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CartService {

    private final CartRepository carrelloRepository;
    private final ScontoRepository scontoRepository;
    private final BoxRepository BoxRepository;

    private DatiSconto calcolaSconto(Box box) {
        BigDecimal prezzoOriginale = box.getPrezzo();
        BigDecimal prezzoScontato = prezzoOriginale;
        Integer percentuale = 0;

        Optional<Sconto> scontoOpt = scontoRepository.findMigliorScontoAttivoPerBox(box.getId(), box.getCategoria());

        if (scontoOpt.isPresent()) {
            Sconto sconto = scontoOpt.get();
            percentuale = sconto.getValore();

            BigDecimal moltiplicatore = BigDecimal.valueOf(100 - percentuale).divide(BigDecimal.valueOf(100));
            prezzoScontato = prezzoOriginale.multiply(moltiplicatore).setScale(2, RoundingMode.HALF_UP);
        }

        return new DatiSconto(prezzoOriginale, prezzoScontato, percentuale);
    }

    private record DatiSconto(BigDecimal originale, BigDecimal scontato, Integer percentuale) {}


    private CartItemDTO mapToCartItemDTO(Carrello carrello) {
        Box box = carrello.getBox();
        DatiSconto sconto = calcolaSconto(box);

        return new CartItemDTO(
                carrello.getId(),
                box.getId(),
                box.getNome(),
                carrello.getQuantita(),
                box.getImmagineUrl(),
                sconto.originale(),
                sconto.scontato(),
                sconto.percentuale()
        );
    }

    public CartDTO getCarrelloDellUtente(Utente utente) {
        List<Carrello> carrelloList = carrelloRepository.findByUtente(utente);


        List<CartItemDTO> items = carrelloList.stream()
                .map(this::mapToCartItemDTO)
                .collect(Collectors.toList());

        int totalItems = items.size();
        int totalQuantity = items.stream()
                .mapToInt(CartItemDTO::quantita)
                .sum();

        // Il calcolo del totale ora è PERFETTO perché prezzoUnitario è già scontato!
        BigDecimal totalPrice = items.stream()
                .map(i -> i.prezzoScontato().multiply(BigDecimal.valueOf(i.quantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(items, totalItems, totalQuantity, totalPrice);
    }

    @Transactional
    public CartDTO aggiungiBoxAlCarrello(Utente utente, Long boxId, Integer quantita) {


        if (quantita == null || quantita <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di zero");
        }

        Box box = BoxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box non trovata con ID: " + boxId));

        // Controllo di Sicurezza extra aggiunto: la Box deve essere in vendita!
        if (Boolean.FALSE.equals(box.getAttivo())) {
            throw new RuntimeException("Questa Box non è attualmente disponibile.");
        }

        Optional<Carrello> rigaEsistente = carrelloRepository.findByUtenteAndBox(utente, box);

        if (rigaEsistente.isPresent()) {
            Carrello riga = rigaEsistente.get();
            riga.setQuantita(riga.getQuantita() + quantita);
            carrelloRepository.save(riga);
        } else {
            Carrello nuovaRiga = new Carrello();
            nuovaRiga.setUtente(utente);
            nuovaRiga.setBox(box);
            nuovaRiga.setQuantita(quantita);
            carrelloRepository.save(nuovaRiga);
        }

        return getCarrelloDellUtente(utente);
    }


    @Transactional
    public void aggiornaQuantita(Utente utente, AggiornaQuantitaDTO request) {
        // 1. Cerca la riga del carrello specifica per quell'utente e quella box
        Carrello riga = carrelloRepository.findByUtenteAndBoxId(utente, request.boxId())
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato nel carrello"));

        // 2. Aggiorna la quantità
        riga.setQuantita(request.quantita());

        // 3. Salva la modifica
        carrelloRepository.save(riga);
    }

    @Transactional
    public void rimuoviProdotto(Utente utente, Long boxId) {
        // 1. Cerca la riga del carrello (così verifichiamo anche che appartenga a quell'utente!)
        Carrello riga = carrelloRepository.findByUtenteAndBoxId(utente, boxId)
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato nel carrello"));

        // 2. Elimina la riga dal database
        carrelloRepository.delete(riga);
    }

}
