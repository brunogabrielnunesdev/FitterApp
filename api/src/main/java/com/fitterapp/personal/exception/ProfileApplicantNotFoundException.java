package com.fitterapp.personal.exception;

public class ProfileApplicantNotFoundException extends RuntimeException {

    public ProfileApplicantNotFoundException() {
        super("Profile applicant was not found");
    }
}
