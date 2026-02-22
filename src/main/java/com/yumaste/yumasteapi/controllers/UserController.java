package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.request.AddToCart;
import com.yumaste.yumasteapi.DTO.response.CartDTO;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.services.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final CartService cartservice;



    public UserController(CartService cartservice) {
        this.cartservice = cartservice;
    }

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

        // Passiamo i dati dal JSON al nostro Service
        CartDTO carrelloAggiornato = cartservice.aggiungiBoxAlCarrello(
                utenteCorrente,
                request.boxId(),
                request.quantita()
        );

        return ResponseEntity.ok(carrelloAggiornato);
    }

}
