package com.fitterapp.auth.exception;

public class AccountNotPendingVerificationException extends RuntimeException {

    public AccountNotPendingVerificationException() {
        super("Account is not pending e-mail verification");
    }
}
