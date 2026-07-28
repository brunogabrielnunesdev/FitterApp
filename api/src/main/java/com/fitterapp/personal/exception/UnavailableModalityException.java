package com.fitterapp.personal.exception;

public class UnavailableModalityException extends RuntimeException {

  public UnavailableModalityException() {
    super("One or more selected modalities are unavailable");
  }
}
