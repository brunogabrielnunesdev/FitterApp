package com.fitterapp.auth.mapper;

import java.net.InetAddress;

import org.springframework.stereotype.Component;

import com.fitterapp.auth.dto.LoginRequest;
import com.fitterapp.auth.dto.LoginResponse;
import com.fitterapp.auth.dto.RegisterRequest;
import com.fitterapp.auth.dto.RegisterResponse;
import com.fitterapp.auth.service.LoginCommand;
import com.fitterapp.auth.service.LoginResult;
import com.fitterapp.auth.service.RegisterCommand;
import com.fitterapp.auth.service.RegistrationResult;

@Component
public class AuthMapper {

    public RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(
                request.fullName(), request.email(), request.phoneNumber(), request.password());
    }

    public RegisterResponse toResponse(RegistrationResult result) {
        return new RegisterResponse(result.userId());
    }

    public LoginCommand toCommand(LoginRequest request, String userAgent, InetAddress ipAddress) {
        return new LoginCommand(request.email(), request.password(), userAgent, ipAddress);
    }

    public LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(
                "Bearer", result.accessToken(), result.refreshToken(), result.expiresInSeconds());
    }
}
