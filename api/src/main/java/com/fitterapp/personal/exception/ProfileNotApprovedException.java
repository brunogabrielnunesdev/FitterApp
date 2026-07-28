package com.fitterapp.personal.exception;

public class ProfileNotApprovedException extends RuntimeException {
  public ProfileNotApprovedException() {
    super("Profile must be approved before publishing");
  }
}
