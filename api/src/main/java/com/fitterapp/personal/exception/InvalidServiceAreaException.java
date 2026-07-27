package com.fitterapp.personal.exception;

public class InvalidServiceAreaException extends RuntimeException {

    public InvalidServiceAreaException() {
        super("Service areas must have a city and a two-letter state code");
    }
}
