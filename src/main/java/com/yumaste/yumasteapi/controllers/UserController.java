package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.request.AddToCart;
import com.yumaste.yumasteapi.DTO.request.CheckoutRequestDTO;
import com.yumaste.yumasteapi.DTO.request.IndirizzoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.CartDTO;
import com.yumaste.yumasteapi.DTO.response.IndirizzoResponseDTO;
import com.yumaste.yumasteapi.DTO.response.OrdineResponseDTO;
import com.yumaste.yumasteapi.DTO.response.UtenteProfileDTO;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.services.CartService;
import com.yumaste.yumasteapi.services.OrderService;
import com.yumaste.yumasteapi.services.UserService;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


}
