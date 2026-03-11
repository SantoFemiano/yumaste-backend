package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.request.*;
import com.yumaste.yumasteapi.DTO.response.*;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.services.CartService;
import com.yumaste.yumasteapi.services.OrderService;
import com.yumaste.yumasteapi.services.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final CartService cartservice;
    private final UserService userService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;



    @GetMapping("cart")
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal Utente user) {
        CartDTO cartDTO = cartservice.getCarrelloDellUtente(user);
        return ResponseEntity.ok(cartDTO);

    }

    @PostMapping("cart/add")
    public ResponseEntity<CartDTO> aggiungiAlCarrello(
            @AuthenticationPrincipal Utente utenteCorrente,
            @RequestBody AddToCart request
    ) {


        CartDTO carrelloAggiornato = cartservice.aggiungiBoxAlCarrello(
                utenteCorrente,
                request.boxId(),
                request.quantita()
        );

        return ResponseEntity.ok(carrelloAggiornato);
    }


    @GetMapping("/profile")
    public ResponseEntity<UtenteProfileDTO> getProfile(Principal principal) {

        return ResponseEntity.ok(userService.getProfilo(principal.getName()));
    }


    @GetMapping("/indirizzi")
    public ResponseEntity<List<IndirizzoResponseDTO>> getIndirizzi(Principal principal) {
        return ResponseEntity.ok(userService.getIndirizziAttivi(principal.getName()));
    }

    @GetMapping("/ordini")
     public ResponseEntity<List<OrdineResponseDTO>> getOrdini(@AuthenticationPrincipal Utente user) {
        return ResponseEntity.ok(orderService.findAllOrdini(user));
    }


    @PostMapping("/insert/indirizzo")
    public ResponseEntity<IndirizzoResponseDTO> addIndirizzo(
            Principal principal,
            @Valid @RequestBody IndirizzoRequestDTO request) {

        IndirizzoResponseDTO nuovoIndirizzo = userService.aggiungiIndirizzo(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovoIndirizzo);
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrdineResponseDTO> checkout(@AuthenticationPrincipal Utente utente, @Valid @RequestBody CheckoutRequestDTO request) {
        OrdineResponseDTO ordine = orderService.checkout(utente,request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ordine);
    }

    @PutMapping("/update/profilo")
    public ResponseEntity<UtenteAggDTO> aggiornaProfilo(@AuthenticationPrincipal Utente utente, @Valid @RequestBody UserUpdateDTO request){
        UtenteAggDTO utenteaggiornato = userService.putProfile(utente, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(utenteaggiornato);
    }


    @PutMapping("/update/profilo/password")
    public ResponseEntity<?> aggiornaProfiloPassword(
            @AuthenticationPrincipal Utente utente, @Valid @RequestBody CambioPasswordDTO request) {
        try {
            UtenteAggDTO utenteaggiornato = userService.putProfilePass(utente, request);
            return ResponseEntity.ok(utenteaggiornato); // 200 OK
        } catch (IllegalArgumentException e) {
            // Se la password vecchia è errata, restituiamo un Bad Request (400)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/ordine/{id}/dettagli")
    public ResponseEntity<List<OrdiniDettagliDTO>> getDettagli(  @AuthenticationPrincipal Utente utente,@PathVariable("id") Long id) {

    List<OrdiniDettagliDTO> dettagli_ordine = orderService.getDettagliOrdini(utente, id);

    return ResponseEntity.ok(dettagli_ordine);

    }

    @PutMapping("/cart/update")
    public ResponseEntity<String> aggiornaQuantitaCarrello(
            @AuthenticationPrincipal Utente utenteCorrente,
            @Valid @RequestBody AggiornaQuantitaDTO request) {

        cartservice.aggiornaQuantita(utenteCorrente, request);
        return ResponseEntity.ok("Quantità aggiornata con successo");
    }

    // --- 2. ENDPOINT PER RIMUOVERE UN PRODOTTO ---
    @DeleteMapping("cart/remove/{boxId}")
    public ResponseEntity<String> rimuoviDalCarrello(
            @AuthenticationPrincipal Utente utenteCorrente,
            @PathVariable("boxId") Long boxId) {

        cartservice.rimuoviProdotto(utenteCorrente, boxId);
        return ResponseEntity.ok("Prodotto rimosso dal carrello");
    }


}
