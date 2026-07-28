package com.fitterapp.personal.exception;

public class ReviewReasonRequiredException extends RuntimeException {
  public ReviewReasonRequiredException() {
    super("A rejection reason is required");
  }
}
