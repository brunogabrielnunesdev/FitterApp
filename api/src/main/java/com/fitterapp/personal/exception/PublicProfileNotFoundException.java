package com.fitterapp.personal.exception;

public class PublicProfileNotFoundException extends RuntimeException {
  public PublicProfileNotFoundException() {
    super("Published profile was not found");
  }
}
