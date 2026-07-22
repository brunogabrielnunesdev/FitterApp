package com.fitterapp.auth.exception;

public class VerificationTokenExpiredException extends RuntimeException {

    public VerificationTokenExpiredException() {
        super("E-mail verification token has expired");
    }
}
