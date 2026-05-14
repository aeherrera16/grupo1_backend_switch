package ec.edu.espe.banquito.notificationservice.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.banquito.notificationservice.dto.PaymentSuccessNotificationRequest;
import ec.edu.espe.banquito.notificationservice.service.PaymentNotificationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class PaymentNotificationController {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentNotificationController.class);

    private final PaymentNotificationService paymentNotificationService;

    public PaymentNotificationController(PaymentNotificationService paymentNotificationService) {
        this.paymentNotificationService = paymentNotificationService;
    }

    @PostMapping("/payment-success")
    public ResponseEntity<Map<String, Object>> sendPaymentSuccessNotification(
            @Valid @RequestBody PaymentSuccessNotificationRequest request) {
        LOG.info("Enviando notificacion RF-05 para paymentDetailId {}", request.getPaymentDetailId());
        paymentNotificationService.sendPaymentSuccessNotification(request);
        return ResponseEntity.ok(Map.of(
                "status", "SENT",
                "paymentDetailId", request.getPaymentDetailId(),
                "timestamp", LocalDateTime.now()));
    }
}
