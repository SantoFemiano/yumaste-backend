package com.yumaste.yumasteapi.services.email;

import com.yumaste.yumasteapi.models.Fattura;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class emailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String from;

    public void sendFatturaOrdine(String toEmail, String nomeCliente, Fattura fattura) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Yumaste — Conferma ordine #" + fattura.getId());
            helper.setText(buildEmailBody(nomeCliente, fattura), true);
        } catch (MessagingException e) {
            throw new RuntimeException("Errore invio email", e);
        }
        mailSender.send(message);
    }

    private String buildEmailBody(String nome, Fattura fattura) {
        return """
            <h2>Ciao %s, grazie per il tuo ordine! 🍕</h2>
            <p>Il tuo ordine <strong>#%d</strong> è stato confermato.</p>
            <p>Totale: <strong>€%.2f</strong></p>
            <p>Riceverai la spedizione a breve.</p>
            <br>
            <p>Il team Yumaste</p>
            """.formatted(nome, fattura.getId(), fattura.getImporto());
    }
}