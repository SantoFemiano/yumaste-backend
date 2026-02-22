package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.response.CartItemDTO;
import com.yumaste.yumasteapi.DTO.response.CartDTO;
import com.yumaste.yumasteapi.mapper.CartItemMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Carrello;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CartService {

    private final CartRepository carrelloRepository;
    private final CartItemMapper cartItemMapper;
    private final BoxRepository BoxRepository;

    public CartDTO getCarrelloDellUtente(Utente utente) {
        List<Carrello> carrelloList = carrelloRepository.findByUtente(utente);

        List<CartItemDTO> items = carrelloList.stream()
                .map(cartItemMapper::toCartItemDTO)
                .collect(Collectors.toList());

        int totalItems = items.size();
        int totalQuantity = items.stream()
                .mapToInt(CartItemDTO::quantita)
                .sum();
        BigDecimal totalPrice = items.stream()
                .map(i -> i.prezzoUnitario().multiply(BigDecimal.valueOf(i.quantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(items, totalItems, totalQuantity, totalPrice);
    }

    @Transactional
    public CartDTO aggiungiBoxAlCarrello(Utente utente, Long boxId, Integer quantita) {

        // 1. Cerchiamo la Box nel database. Se non esiste, blocchiamo tutto.
        Box box = BoxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box non trovata con ID: " + boxId));

        // 2. Controlliamo se questa Box è già nel carrello di questo utente
        Optional<Carrello> rigaEsistente = carrelloRepository.findByUtenteAndBox(utente, box);

        if (rigaEsistente.isPresent()) {
            // CASO A: La Box c'è già. Aumentiamo solo la quantità.
            Carrello riga = rigaEsistente.get();
            riga.setQuantita(riga.getQuantita() + quantita);
            carrelloRepository.save(riga);
        } else {
            // CASO B: La Box non c'è. Creiamo una nuova riga.
            Carrello nuovaRiga = new Carrello();
            nuovaRiga.setUtente(utente);
            nuovaRiga.setBox(box);
            nuovaRiga.setQuantita(quantita);


            carrelloRepository.save(nuovaRiga);
        }

        // 3. Magia del riutilizzo: restituiamo il carrello ricalcolato
        // chiamando il metodo che avevamo già scritto!
        return getCarrelloDellUtente(utente);
    }

}
