package com.fitterapp.auth.service.register;

public record RegisterCommand(
        String fullName,
        String email,
        String phoneNumber,
        String password) {
}
