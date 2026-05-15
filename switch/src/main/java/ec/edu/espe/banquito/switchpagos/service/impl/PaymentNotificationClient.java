package ec.edu.espe.banquito.switchpagos.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.dto.PaymentSuccessNotificationRequestDTO;
import ec.edu.espe.banquito.switchpagos.service.IPaymentNotificationClient;

@Service
public class PaymentNotificationClient implements IPaymentNotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationClient.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromEmail;

    @Autowired
    public PaymentNotificationClient(JavaMailSender mailSender,
                                     @Value("${app.notification.enabled:true}") boolean enabled,
                                     @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromEmail = fromEmail;
    }

    @Override
    public boolean sendPaymentSuccessNotification(PaymentSuccessNotificationRequestDTO request) {
        if (!enabled) {
            logger.warn("Notificaciones RF-05 deshabilitadas; no se enviara paymentDetailId {}",
                    request.getPaymentDetailId());
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getBeneficiaryEmail());
            message.setSubject("Notificación de Pago Exitoso - " + request.getCompanyName());
            
            String emailBody = buildEmailBody(request);
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Notificación RF-05 enviada exitosamente a paymentDetailId {}", request.getPaymentDetailId());
            return true;
        } catch (Exception e) {
            logger.error("Error enviando notificacion RF-05 para paymentDetailId {}: {}",
                    request.getPaymentDetailId(), e.getMessage());
            return false;
        }
    }

    private String buildEmailBody(PaymentSuccessNotificationRequestDTO request) {
        StringBuilder body = new StringBuilder();
        body.append("Estimado/a ").append(request.getBeneficiaryName()).append(",\n\n");
        body.append("Le informamos que su pago ha sido procesado exitosamente.\n\n");
        body.append("Detalle del pago:\n");
        body.append("- Empresa emisora: ").append(request.getCompanyName()).append("\n");
        body.append("- Monto acreditado: $").append(request.getAmount()).append("\n");
        body.append("- Concepto: ").append(request.getConcept()).append("\n\n");
        body.append("Si tiene alguna pregunta, por favor contacte a la empresa emisora.\n\n");
        body.append("Atentamente,\n");
        body.append(request.getCompanyName());
        return body.toString();
    }
}
