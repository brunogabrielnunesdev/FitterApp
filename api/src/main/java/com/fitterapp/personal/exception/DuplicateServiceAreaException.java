package com.fitterapp.personal.exception;

public class DuplicateServiceAreaException extends RuntimeException {

    public DuplicateServiceAreaException() {
        super("Duplicated service area");
    }
}
