package com.yumaste.yumasteapi.listeners;

import com.yumaste.yumasteapi.events.OrderCreatedEvent;
import com.yumaste.yumasteapi.models.Fattura;
import com.yumaste.yumasteapi.models.Ordine;
import com.yumaste.yumasteapi.repositories.FatturaRepository;
import com.yumaste.yumasteapi.repositories.OrdineRepository;
import com.yumaste.yumasteapi.services.email.emailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final emailService emailService;
    private final OrdineRepository ordineRepository;
    private final FatturaRepository fatturaRepository;

    @Transactional
    @KafkaListener(topics = "order-created-topic", groupId = "yumaste-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Ricevuto evento Kafka per l'ordine ID: {}", event.getOrdineId());

        try {

            Ordine ordine = ordineRepository.findById(event.getOrdineId())
                    .orElseThrow(() -> new RuntimeException("Ordine non trovato"));

            Fattura fattura = fatturaRepository.findByOrdine(ordine)
                    .orElseThrow(() -> new RuntimeException("Fattura non trovata"));


            emailService.sendFatturaOrdine(
                    event.getEmailUtente(),
                    event.getNomeCliente(),
                    fattura
            );

            log.info("Email inviata con successo in background per l'ordine {}", event.getOrdineId());

        } catch (Exception e) {
            log.error("Errore durante l'invio dell'email per l'ordine {}: {}", event.getOrdineId(), e.getMessage());
        }
    }
}