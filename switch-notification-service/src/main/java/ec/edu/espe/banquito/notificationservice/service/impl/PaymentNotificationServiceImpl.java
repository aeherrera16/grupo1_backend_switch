package ec.edu.espe.banquito.notificationservice.service.impl;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ec.edu.espe.banquito.notificationservice.dto.PaymentSuccessNotificationRequest;
import ec.edu.espe.banquito.notificationservice.service.PaymentNotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class PaymentNotificationServiceImpl implements PaymentNotificationService {

    private static final Locale ECUADOR_LOCALE = Locale.forLanguageTag("es-EC");

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;

    public PaymentNotificationServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress,
            @Value("${app.mail.from-name:Switch Pagos}") String fromName) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Override
    public void sendPaymentSuccessNotification(PaymentSuccessNotificationRequest request) {
        if (!StringUtils.hasText(fromAddress)) {
            throw new IllegalStateException("MAIL_FROM no esta configurado para enviar notificaciones");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(new InternetAddress(fromAddress, fromName));
            helper.setTo(request.getBeneficiaryEmail());
            helper.setSubject("Pago acreditado - " + request.getCompanyName());
            helper.setText(buildBody(request), false);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            throw new IllegalStateException("No se pudo enviar la notificacion de pago", e);
        }
    }

    private String buildBody(PaymentSuccessNotificationRequest request) {
        String amount = NumberFormat.getCurrencyInstance(ECUADOR_LOCALE).format(request.getAmount());
        return """
                Estimado/a %s,

                Se ha acreditado un pago en su cuenta.

                Monto acreditado: %s
                Concepto: %s
                Empresa emisora: %s

                Este mensaje fue generado automaticamente por Switch Pagos.
                """.formatted(
                request.getBeneficiaryName(),
                amount,
                request.getConcept(),
                request.getCompanyName());
    }
}
