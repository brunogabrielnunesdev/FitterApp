package com.fitterapp.personal.exception;

public class ProfileNotPendingReviewException extends RuntimeException {
    public ProfileNotPendingReviewException() { super("Profile revision is not awaiting review"); }
}
