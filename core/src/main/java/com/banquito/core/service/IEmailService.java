package com.banquito.core.service;

public interface IEmailService {
    void sendStatusChangeEmail(String to, String accountNumber, String newStatus);
}