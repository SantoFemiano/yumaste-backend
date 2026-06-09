package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.AggiornaQuantitaDTO;
import com.yumaste.yumasteapi.dto.response.CartDTO;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Carrello;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.CartRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private ScontoRepository scontoRepository;
    @Mock private BoxRepository boxRepository;
    @Mock private UtenteRepository utenteRepository;

    @InjectMocks private CartService cartService;

    private Utente utente;
    private Box box;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setEmail("test@yumaste.it");

        box = new Box();
        box.setId(10L);
        box.setNome("Box Test");
        box.setPrezzo(new BigDecimal("15.00"));
        box.setAttivo(true);
    }

    // ---- getCarrelloDellUtente ----

    @Test
    @DisplayName("getCarrelloDellUtente - carrello vuoto restituisce totali a zero")
    void getCarrello_empty() {
        // Carrello vuoto: scontoRepository non viene mai chiamato, nessun stubbing necessario
        when(cartRepository.findByUtente(utente)).thenReturn(List.of());

        CartDTO dto = cartService.getCarrelloDellUtente(utente);

        assertThat(dto.items()).isEmpty();
        assertThat(dto.totalItems()).isZero();
        assertThat(dto.totalQuantity()).isZero();
        assertThat(dto.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getCarrelloDellUtente - un articolo senza sconto")
    void getCarrello_oneItemNoDiscount() {
        Carrello riga = new Carrello();
        riga.setId(1L);
        riga.setBox(box);
        riga.setQuantita(2);
        riga.setUtente(utente);

        when(cartRepository.findByUtente(utente)).thenReturn(List.of(riga));
        when(scontoRepository.findMigliorScontoAttivoPerBox(box.getId(), box.getCategoria()))
                .thenReturn(Optional.empty());

        CartDTO dto = cartService.getCarrelloDellUtente(utente);

        assertThat(dto.totalItems()).isEqualTo(1);
        assertThat(dto.totalQuantity()).isEqualTo(2);
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    // ---- aggiungiBoxAlCarrello ----

    @Test
    @DisplayName("aggiungiBoxAlCarrello - quantita null lancia IllegalArgumentException")
    void addToCart_nullQuantity() {
        assertThatThrownBy(() -> cartService.aggiungiBoxAlCarrello(utente, 10L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("aggiungiBoxAlCarrello - quantita zero lancia IllegalArgumentException")
    void addToCart_zeroQuantity() {
        assertThatThrownBy(() -> cartService.aggiungiBoxAlCarrello(utente, 10L, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("aggiungiBoxAlCarrello - box non trovata lancia ResourceNotFoundException")
    void addToCart_boxNotFound() {
        when(boxRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.aggiungiBoxAlCarrello(utente, 10L, 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("aggiungiBoxAlCarrello - box non attiva lancia BusinessException")
    void addToCart_boxNotActive() {
        box.setAttivo(false);
        when(boxRepository.findById(10L)).thenReturn(Optional.of(box));
        assertThatThrownBy(() -> cartService.aggiungiBoxAlCarrello(utente, 10L, 1))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("aggiungiBoxAlCarrello - nuova riga creata se non esiste")
    void addToCart_newRow() {
        when(boxRepository.findById(10L)).thenReturn(Optional.of(box));
        when(cartRepository.findByUtenteAndBox(utente, box)).thenReturn(Optional.empty());
        // Dopo il save, getCarrelloDellUtente viene chiamato: il carrello ha 1 item
        Carrello nuova = new Carrello();
        nuova.setBox(box);
        nuova.setQuantita(3);
        nuova.setUtente(utente);
        when(cartRepository.findByUtente(utente)).thenReturn(List.of(nuova));
        when(scontoRepository.findMigliorScontoAttivoPerBox(box.getId(), box.getCategoria()))
                .thenReturn(Optional.empty());

        cartService.aggiungiBoxAlCarrello(utente, 10L, 3);

        verify(cartRepository, times(1)).save(any(Carrello.class));
    }

    @Test
    @DisplayName("aggiungiBoxAlCarrello - riga esistente aggiorna quantita")
    void addToCart_existingRow() {
        Carrello esistente = new Carrello();
        esistente.setBox(box);
        esistente.setQuantita(2);
        esistente.setUtente(utente);

        when(boxRepository.findById(10L)).thenReturn(Optional.of(box));
        when(cartRepository.findByUtenteAndBox(utente, box)).thenReturn(Optional.of(esistente));
        when(cartRepository.findByUtente(utente)).thenReturn(List.of(esistente));
        when(scontoRepository.findMigliorScontoAttivoPerBox(any(), any())).thenReturn(Optional.empty());

        cartService.aggiungiBoxAlCarrello(utente, 10L, 3);

        assertThat(esistente.getQuantita()).isEqualTo(5);
        verify(cartRepository, times(1)).save(esistente);
    }

    // ---- rimuoviProdotto ----

    @Test
    @DisplayName("rimuoviProdotto - prodotto non nel carrello lancia ResourceNotFoundException")
    void removeProduct_notFound() {
        when(cartRepository.findByUtenteAndBoxId(utente, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.rimuoviProdotto(utente, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("rimuoviProdotto - rimozione avvenuta con successo")
    void removeProduct_success() {
        Carrello riga = new Carrello();
        when(cartRepository.findByUtenteAndBoxId(utente, 10L)).thenReturn(Optional.of(riga));

        cartService.rimuoviProdotto(utente, 10L);

        verify(cartRepository).delete(riga);
    }

    // ---- aggiornaQuantita ----

    @Test
    @DisplayName("aggiornaQuantita - prodotto non trovato lancia ResourceNotFoundException")
    void updateQuantity_notFound() {
        AggiornaQuantitaDTO req = new AggiornaQuantitaDTO(10L, 5);
        when(cartRepository.findByUtenteAndBoxId(utente, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.aggiornaQuantita(utente, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("aggiornaQuantita - quantita aggiornata correttamente")
    void updateQuantity_success() {
        Carrello riga = new Carrello();
        riga.setQuantita(1);
        AggiornaQuantitaDTO req = new AggiornaQuantitaDTO(10L, 7);
        when(cartRepository.findByUtenteAndBoxId(utente, 10L)).thenReturn(Optional.of(riga));

        cartService.aggiornaQuantita(utente, req);

        assertThat(riga.getQuantita()).isEqualTo(7);
        verify(cartRepository).save(riga);
    }

    // ---- getCarrelloUtenteById ----

    @Test
    @DisplayName("getCarrelloUtenteById - utente non trovato lancia ResourceNotFoundException")
    void getCartById_userNotFound() {
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.getCarrelloUtenteById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCarrelloUtenteById - utente trovato restituisce carrello")
    void getCartById_success() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(cartRepository.findByUtente(utente)).thenReturn(List.of());

        CartDTO dto = cartService.getCarrelloUtenteById(1L);

        assertThat(dto).isNotNull();
    }
}
