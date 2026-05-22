package com.yumaste.yumasteapi.events;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long ordineId;
    private String emailUtente;
    private String nomeCliente;
}
