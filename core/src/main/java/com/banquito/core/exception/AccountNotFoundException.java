package com.banquito.core.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String numeroCuenta) {
        super("Cuenta no encontrada: " + numeroCuenta);
    }
}
