package com.banquito.core.exception;

public class InactiveAccountException extends RuntimeException {

    public InactiveAccountException(String numeroCuenta) {
        super("La cuenta no está activa: " + numeroCuenta);
    }
}
