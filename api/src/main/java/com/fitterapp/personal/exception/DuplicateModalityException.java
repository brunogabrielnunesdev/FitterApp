package com.fitterapp.personal.exception;

public class DuplicateModalityException extends RuntimeException {
  public DuplicateModalityException() {
    super("A modality with the same name or slug already exists");
  }
}
