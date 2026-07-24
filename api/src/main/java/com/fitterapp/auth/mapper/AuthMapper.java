package com.fitterapp.auth.mapper;

import java.net.InetAddress;

import org.springframework.stereotype.Component;

import com.fitterapp.auth.dto.login.LoginRequestDto;
import com.fitterapp.auth.dto.login.LoginResponseDto;
import com.fitterapp.auth.dto.register.RegisterRequestDto;
import com.fitterapp.auth.dto.register.RegisterResponseDto;
import com.fitterapp.auth.service.login.LoginCommand;
import com.fitterapp.auth.service.login.LoginResult;
import com.fitterapp.auth.service.register.RegisterCommand;
import com.fitterapp.auth.service.register.RegisterResult;

@Component
public class AuthMapper {

    public RegisterCommand toCommand(RegisterRequestDto request) {
        return new RegisterCommand(
                request.fullName(), request.email(), request.phoneNumber(), request.password());
    }

    public RegisterResponseDto toResponse(RegisterResult result) {
        return new RegisterResponseDto(result.userId());
    }

    public LoginCommand toCommand(LoginRequestDto request, String userAgent, InetAddress ipAddress) {
        return new LoginCommand(request.email(), request.password(), userAgent, ipAddress);
    }

    public LoginResponseDto toResponse(LoginResult result) {
        return new LoginResponseDto(
                "Bearer", result.accessToken(), result.refreshToken(), result.expiresInSeconds());
    }
}
