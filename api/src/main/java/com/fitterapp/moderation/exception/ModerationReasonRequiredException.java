package com.fitterapp.moderation.exception;

public class ModerationReasonRequiredException extends RuntimeException {
  public ModerationReasonRequiredException() {
    super("A moderation reason is required");
  }
}
