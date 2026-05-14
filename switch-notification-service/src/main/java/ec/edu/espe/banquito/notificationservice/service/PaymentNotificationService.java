package ec.edu.espe.banquito.notificationservice.service;

import ec.edu.espe.banquito.notificationservice.dto.PaymentSuccessNotificationRequest;

public interface PaymentNotificationService {

    void sendPaymentSuccessNotification(PaymentSuccessNotificationRequest request);
}
