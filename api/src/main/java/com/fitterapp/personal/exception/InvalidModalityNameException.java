package com.fitterapp.personal.exception;

public class InvalidModalityNameException extends RuntimeException {
  public InvalidModalityNameException() {
    super("Modality name must contain at least one letter or number");
  }
}
