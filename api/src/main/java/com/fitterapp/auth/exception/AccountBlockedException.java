package com.fitterapp.auth.exception;

public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException() {
        super("Account is blocked");
    }
}
