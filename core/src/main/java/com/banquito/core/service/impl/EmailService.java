package com.banquito.core.service.impl;

import com.banquito.core.service.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.from-name}")
    private String mailFromName;

    @Async
    @Override
    public void sendStatusChangeEmail(String to, String accountNumber, String newStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(to);
            helper.setSubject("Aviso de Seguridad: Cambio de Estado en su Cuenta BanQuito");
            
            String maskedAccount = "***" + accountNumber.substring(Math.max(0, accountNumber.length() - 4));
            
            String text = "Estimado cliente,\n\n" +
                    "Le informamos que por motivos de seguridad, su cuenta terminada en " + maskedAccount + 
                    " ha cambiado al estado: " + newStatus + ".\n\n" +
                    "Si usted no solicitó ni reconoce esta acción, por favor comuníquese de inmediato con nuestro soporte telefónico.\n\n" +
                    "Atentamente,\nBanco BanQuito S.A.";

            helper.setText(text, false); 

            mailSender.send(message);
            log.info("Correo de notificación de estado ({}) enviado exitosamente a {} vía Brevo", newStatus, to);
            
        } catch (Exception e) {
            log.error("Error al intentar enviar el correo vía Brevo a {}: {}", to, e.getMessage());
        }
    }
}