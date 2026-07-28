package com.fitterapp.personal.exception;

public class ProfileAlreadyExistsException extends RuntimeException {

  public ProfileAlreadyExistsException() {
    super("User already has a professional profile");
  }
}
