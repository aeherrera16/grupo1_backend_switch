package ec.edu.espe.banquito.switchpagos.service;

import ec.edu.espe.banquito.switchpagos.dto.PaymentSuccessNotificationRequestDTO;

public interface IPaymentNotificationClient {

    boolean sendPaymentSuccessNotification(PaymentSuccessNotificationRequestDTO request);
}
