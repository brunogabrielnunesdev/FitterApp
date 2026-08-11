package com.fitterapp.personal.exception;

public class ModalityNotFoundException extends RuntimeException {
  public ModalityNotFoundException() {
    super("Modality not found");
  }
}
