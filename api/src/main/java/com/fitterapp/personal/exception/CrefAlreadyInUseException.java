package com.fitterapp.personal.exception;

public class CrefAlreadyInUseException extends RuntimeException {

    public CrefAlreadyInUseException() {
        super("CREF registration code is already linked to another profile");
    }
}
