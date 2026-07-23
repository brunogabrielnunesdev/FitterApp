package com.fitterapp.auth.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitterapp.auth.dto.ConfirmEmailRequest;
import com.fitterapp.auth.dto.LoginRequest;
import com.fitterapp.auth.dto.LoginResponse;
import com.fitterapp.auth.dto.RegisterRequest;
import com.fitterapp.auth.dto.RegisterResponse;
import com.fitterapp.auth.dto.ResendConfirmationRequest;
import com.fitterapp.auth.mapper.AuthMapper;
import com.fitterapp.auth.service.ConfirmEmailService;
import com.fitterapp.auth.service.LoginService;
import com.fitterapp.auth.service.RegisterService;
import com.fitterapp.auth.service.ResendConfirmationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final ConfirmEmailService confirmEmailService;
    private final ResendConfirmationService resendConfirmationService;
    private final AuthMapper mapper;

    public AuthController(
            RegisterService registerService,
            LoginService loginService,
            ConfirmEmailService confirmEmailService,
            ResendConfirmationService resendConfirmationService,
            AuthMapper mapper) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.confirmEmailService = confirmEmailService;
        this.resendConfirmationService = resendConfirmationService;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var command = mapper.toCommand(request);
        var result = registerService.register(command);
        var response = mapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        var command = mapper.toCommand(
                request,
                servletRequest.getHeader("User-Agent"),
                parseAddress(servletRequest.getRemoteAddr()));
        var result = loginService.login(command);
        var response = mapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<Void> confirmEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        confirmEmailService.confirm(request.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email/resend")
    public ResponseEntity<Void> resendConfirmation(
            @Valid @RequestBody ResendConfirmationRequest request) {
        resendConfirmationService.resend(request.email());
        return ResponseEntity.accepted().build();
    }

    private InetAddress parseAddress(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("Invalid client IP address", exception);
        }
    }
}
