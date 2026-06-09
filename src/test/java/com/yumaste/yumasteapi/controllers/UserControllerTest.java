package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.dto.request.*;
import com.yumaste.yumasteapi.dto.response.*;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.services.CartService;
import com.yumaste.yumasteapi.services.OrderService;
import com.yumaste.yumasteapi.services.UserService;
import com.yumaste.yumasteapi.services.ai.AiDescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private CartService cartservice;
    @Mock private UserService userService;
    @Mock private OrderService orderService;
    @Mock private AiDescriptionService aiDescriptionService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setEmail("mario@yumaste.it");
    }

    // --- getProfile ---

    @Test
    @DisplayName("getProfile - restituisce 200 con profilo utente")
    void getProfile_returns200() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("mario@yumaste.it");
        UtenteProfileDTO dto = mock(UtenteProfileDTO.class);
        when(userService.getProfilo("mario@yumaste.it")).thenReturn(dto);

        ResponseEntity<UtenteProfileDTO> resp = userController.getProfile(principal);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    // --- getIndirizzi ---

    @Test
    @DisplayName("getIndirizzi - restituisce 200 con lista indirizzi attivi")
    void getIndirizzi_returns200() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("mario@yumaste.it");
        IndirizzoResponseDTO addr = mock(IndirizzoResponseDTO.class);
        when(userService.getIndirizziAttivi("mario@yumaste.it")).thenReturn(List.of(addr));

        ResponseEntity<List<IndirizzoResponseDTO>> resp = userController.getIndirizzi(principal);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(addr);
    }

    // --- getOrdini ---

    @Test
    @DisplayName("getOrdini - restituisce 200 con lista ordini")
    void getOrdini_returns200() {
        OrdineResponseDTO ordine = mock(OrdineResponseDTO.class);
        when(orderService.findAllOrdini(utente)).thenReturn(List.of(ordine));

        ResponseEntity<List<OrdineResponseDTO>> resp = userController.getOrdini(utente);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(ordine);
    }

    // --- addIndirizzo ---

    @Test
    @DisplayName("addIndirizzo - crea indirizzo e restituisce 201")
    void addIndirizzo_returns201() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("mario@yumaste.it");
        IndirizzoRequestDTO req = mock(IndirizzoRequestDTO.class);
        IndirizzoResponseDTO dto = mock(IndirizzoResponseDTO.class);
        when(userService.aggiungiIndirizzo("mario@yumaste.it", req)).thenReturn(dto);

        ResponseEntity<IndirizzoResponseDTO> resp = userController.addIndirizzo(principal, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    // --- deleteIndirizzo ---

    @Test
    @DisplayName("deleteIndirizzo - esegue soft delete e restituisce 204")
    void deleteIndirizzo_returns204() {
        doNothing().when(userService).deleteIndirizzo(1L, utente);

        ResponseEntity<Void> resp = userController.deleteIndirizzo(utente, 1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteIndirizzo(1L, utente);
    }

    // --- getCart ---

    @Test
    @DisplayName("getCart - restituisce 200 con carrello")
    void getCart_returns200() {
        CartDTO cart = mock(CartDTO.class);
        when(cartservice.getCarrelloDellUtente(utente)).thenReturn(cart);

        ResponseEntity<CartDTO> resp = userController.getCart(utente);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(cart);
    }

    // --- aggiungiAlCarrello ---

    @Test
    @DisplayName("aggiungiAlCarrello - restituisce 200 con carrello aggiornato")
    void aggiungiAlCarrello_returns200() {
        AddToCart req = mock(AddToCart.class);
        when(req.boxId()).thenReturn(10L);
        when(req.quantita()).thenReturn(2);
        CartDTO cart = mock(CartDTO.class);
        when(cartservice.aggiungiBoxAlCarrello(utente, 10L, 2)).thenReturn(cart);

        ResponseEntity<CartDTO> resp = userController.aggiungiAlCarrello(utente, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(cart);
    }

    // --- checkout ---

    @Test
    @DisplayName("checkout - restituisce 202 con ordine creato")
    void checkout_returns202() {
        CheckoutRequestDTO req = mock(CheckoutRequestDTO.class);
        OrdineResponseDTO ordine = mock(OrdineResponseDTO.class);
        when(orderService.checkout(utente, req)).thenReturn(ordine);

        ResponseEntity<OrdineResponseDTO> resp = userController.checkout(utente, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(resp.getBody()).isEqualTo(ordine);
    }

    // --- aggiornaProfilo ---

    @Test
    @DisplayName("aggiornaProfilo - restituisce 202 con profilo aggiornato")
    void aggiornaProfilo_returns202() {
        UserUpdateDTO req = mock(UserUpdateDTO.class);
        UtenteAggDTO dto = mock(UtenteAggDTO.class);
        when(userService.putProfile(utente, req)).thenReturn(dto);

        ResponseEntity<UtenteAggDTO> resp = userController.aggiornaProfilo(utente, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    // --- aggiornaProfiloPassword ---

    @Test
    @DisplayName("aggiornaProfiloPassword - restituisce 200 con profilo aggiornato")
    void aggiornaProfiloPassword_returns200() {
        CambioPasswordDTO req = mock(CambioPasswordDTO.class);
        UtenteAggDTO dto = mock(UtenteAggDTO.class);
        when(userService.putProfilePass(utente, req)).thenReturn(dto);

        ResponseEntity<UtenteAggDTO> resp = userController.aggiornaProfiloPassword(utente, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    // --- getDettagli ordine ---

    @Test
    @DisplayName("getDettagli - restituisce 200 con dettagli ordine")
    void getDettagli_returns200() {
        OrdiniDettagliDTO det = mock(OrdiniDettagliDTO.class);
        when(orderService.getDettagliOrdini(utente, 5L)).thenReturn(List.of(det));

        ResponseEntity<List<OrdiniDettagliDTO>> resp = userController.getDettagli(utente, 5L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(det);
    }

    // --- aggiornaQuantitaCarrello ---

    @Test
    @DisplayName("aggiornaQuantitaCarrello - restituisce 200 con messaggio")
    void aggiornaQuantitaCarrello_returns200() {
        AggiornaQuantitaDTO req = mock(AggiornaQuantitaDTO.class);
        doNothing().when(cartservice).aggiornaQuantita(utente, req);

        ResponseEntity<String> resp = userController.aggiornaQuantitaCarrello(utente, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("aggiornata");
    }

    // --- rimuoviDalCarrello ---

    @Test
    @DisplayName("rimuoviDalCarrello - restituisce 200 con messaggio")
    void rimuoviDalCarrello_returns200() {
        doNothing().when(cartservice).rimuoviProdotto(utente, 3L);

        ResponseEntity<String> resp = userController.rimuoviDalCarrello(utente, 3L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("rimosso");
    }

    // --- consigliaBoxDaOrdini ---

    @Test
    @DisplayName("consigliaBoxDaOrdini - restituisce 200 con raccomandazione AI")
    void consigliaBoxDaOrdini_returns200() {
        AiRecommendationResponseDTO dto = mock(AiRecommendationResponseDTO.class);
        when(aiDescriptionService.consigliaBoxDaOrdini(1L)).thenReturn(dto);

        ResponseEntity<AiRecommendationResponseDTO> resp = userController.consigliaBoxDaOrdini(utente);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(dto);
    }
}
