package ec.edu.espe.banquito.switchpagos.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ec.edu.espe.banquito.switchpagos.dto.PaymentSuccessNotificationRequestDTO;
import ec.edu.espe.banquito.switchpagos.service.IPaymentNotificationClient;

@Service
public class PaymentNotificationClient implements IPaymentNotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationClient.class);

    private final RestClient restClient;
    private final boolean enabled;

    public PaymentNotificationClient(
            @Value("${app.notification.base-url}") String notificationBaseUrl,
            @Value("${app.notification.enabled:true}") boolean enabled) {
        this.restClient = RestClient.builder()
                .baseUrl(notificationBaseUrl)
                .build();
        this.enabled = enabled;
    }

    @Override
    public boolean sendPaymentSuccessNotification(PaymentSuccessNotificationRequestDTO request) {
        if (!enabled) {
            logger.warn("Notificaciones RF-05 deshabilitadas; no se enviara paymentDetailId {}",
                    request.getPaymentDetailId());
            return false;
        }

        try {
            restClient.post()
                    .uri("/api/notifications/payment-success")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            logger.error("Error enviando notificacion RF-05 para paymentDetailId {}: {}",
                    request.getPaymentDetailId(), e.getMessage());
            return false;
        }
    }
}
