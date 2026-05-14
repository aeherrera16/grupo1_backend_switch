package com.banquito.core.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String identificacion) {
        super("Cliente no encontrado: " + identificacion);
    }
}
