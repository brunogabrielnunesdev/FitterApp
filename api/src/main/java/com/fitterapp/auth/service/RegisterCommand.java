package com.fitterapp.auth.service;

public record RegisterCommand(
        String fullName,
        String email,
        String phoneNumber,
        String password) {
}
