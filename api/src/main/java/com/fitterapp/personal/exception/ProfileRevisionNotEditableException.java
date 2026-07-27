package com.fitterapp.personal.exception;

public class ProfileRevisionNotEditableException extends RuntimeException {

    public ProfileRevisionNotEditableException() {
        super("Current profile revision cannot be edited");
    }
}
