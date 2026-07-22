package com.fitterapp.auth.exception;

public class VerificationTokenAlreadyUsedException extends RuntimeException {

    public VerificationTokenAlreadyUsedException() {
        super("E-mail verification token has already been used");
    }
}
