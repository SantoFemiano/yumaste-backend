package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.CheckoutRequestDTO;
import com.yumaste.yumasteapi.DTO.response.CartDTO;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

//utilizzo di j-unit con mockito
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    //creazione mock
    @Mock private CartService cartService;
    @Mock private CartRepository cartRepository;
    @Mock private BoxRepository boxRepository;
    @Mock private OrdineRepository ordineRepository;
    @Mock private DettaglioOrdineRepository dettaglioOrdineRepository;
    @Mock private SpedizioneRepository spedizioneRepository;
    @Mock private FatturaRepository fatturaRepository;
    @Mock private IndirizzoUtenteRepository indirizzoRepository;

    //dependecy injection dei mock nei service reali
    @InjectMocks
    private OrderService orderService;


    @Test
    void checkout_ConCarrelloVuoto_LanciaEccezione() {

        Utente fintoUtente = new Utente();
        fintoUtente.setId(1L);

        CheckoutRequestDTO fintaRichiesta = new CheckoutRequestDTO(1L, "CARTA_CREDITO");

        CartDTO carrelloVuoto = new CartDTO(Collections.emptyList(), 0, 0, BigDecimal.ZERO);

        when(cartService.getCarrelloDellUtente(fintoUtente)).thenReturn(carrelloVuoto);


        //esecuzione e verifica


        RuntimeException eccezioneLanciata = assertThrows(RuntimeException.class, () -> {
            orderService.checkout(fintoUtente, fintaRichiesta);
        });


        assertEquals("Impossibile effettuare l'ordine: il carrello è vuoto.", eccezioneLanciata.getMessage());


        verifyNoInteractions(ordineRepository);
        verifyNoInteractions(spedizioneRepository);
    }
}