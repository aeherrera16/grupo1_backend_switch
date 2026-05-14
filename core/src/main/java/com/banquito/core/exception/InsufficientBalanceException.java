package com.banquito.core.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String numeroCuenta) {
        super("Saldo insuficiente en la cuenta: " + numeroCuenta);
    }
}
