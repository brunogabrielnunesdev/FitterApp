package com.fitterapp.auth.exception;

public class InvalidVerificationTokenException extends RuntimeException {

    public InvalidVerificationTokenException() {
        super("E-mail verification token is invalid");
    }
}
