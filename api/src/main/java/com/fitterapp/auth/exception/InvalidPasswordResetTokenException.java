package com.fitterapp.auth.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
  public InvalidPasswordResetTokenException() {
    super("Password reset token is invalid, expired, or already used");
  }
}
