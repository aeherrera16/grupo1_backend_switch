package com.banquito.core.exception;

public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(String uuid) {
        super("Transacción duplicada para el UUID: " + uuid);
    }
}
