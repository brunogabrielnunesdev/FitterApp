package com.fitterapp.personal.exception;

public class IncompleteProfileException extends RuntimeException {
    public IncompleteProfileException() {
        super("Complete the required profile information before submitting it for review");
    }
}
