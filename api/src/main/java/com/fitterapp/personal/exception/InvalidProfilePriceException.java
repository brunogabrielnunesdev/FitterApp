package com.fitterapp.personal.exception;

public class InvalidProfilePriceException extends RuntimeException {

    public InvalidProfilePriceException() {
        super("Starting price and price unit must be informed together");
    }
}
