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
            logger.warn("Notifications are disabled; paymentDetailId {} will not be sent",
                    request.getPaymentDetailId());
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getBeneficiaryEmail());
            message.setSubject("Successful Payment Notification - " + request.getCompanyName());

            String emailBody = buildEmailBody(request);
            message.setText(emailBody);

            mailSender.send(message);
            logger.info("Notification sent successfully for paymentDetailId {}", request.getPaymentDetailId());
            return true;
        } catch (Exception e) {
            logger.error("Error sending notification for paymentDetailId {}: {}",
                    request.getPaymentDetailId(), e.getMessage());
            return false;
        }
    }

    // RF-05: builds the immediate beneficiary email payload.
    private String buildEmailBody(PaymentSuccessNotificationRequestDTO request) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(request.getBeneficiaryName()).append(",\n\n");
        body.append("Your payment has been processed successfully.\n\n");
        body.append("Payment details:\n");
        body.append("- Sending company: ").append(request.getCompanyName()).append("\n");
        body.append("- Credited amount: $").append(request.getAmount()).append("\n");
        body.append("- Concept: ").append(request.getConcept()).append("\n\n");
        body.append("If you have any questions, please contact the sending company.\n\n");
        body.append("Best regards,\n");
        body.append(request.getCompanyName());
        return body.toString();
    }
}
