package com.fitterapp.personal.exception;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException() {
        super("Professional profile was not found");
    }
}
