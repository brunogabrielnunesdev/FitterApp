package com.fitterapp.auth.controller;

import com.fitterapp.auth.dto.emailconfirm.ConfirmEmailRequestDto;
import com.fitterapp.auth.dto.emailconfirm.ResendConfirmationRequestDto;
import com.fitterapp.auth.dto.login.LoginRequestDto;
import com.fitterapp.auth.dto.login.LoginResponseDto;
import com.fitterapp.auth.dto.password.RequestPasswordResetDto;
import com.fitterapp.auth.dto.password.ResetPasswordDto;
import com.fitterapp.auth.dto.register.RegisterRequestDto;
import com.fitterapp.auth.dto.register.RegisterResponseDto;
import com.fitterapp.auth.dto.session.RefreshTokenRequestDto;
import com.fitterapp.auth.mapper.AuthMapper;
import com.fitterapp.auth.service.emailconfirm.ConfirmEmailService;
import com.fitterapp.auth.service.emailconfirm.ResendConfirmationService;
import com.fitterapp.auth.service.login.LoginService;
import com.fitterapp.auth.service.password.RequestPasswordResetService;
import com.fitterapp.auth.service.password.ResetPasswordService;
import com.fitterapp.auth.service.register.RegisterService;
import com.fitterapp.auth.service.session.LogoutService;
import com.fitterapp.auth.service.session.RefreshSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterService registerService;
  private final LoginService loginService;
  private final ConfirmEmailService confirmEmailService;
  private final ResendConfirmationService resendConfirmationService;
  private final RefreshSessionService refreshSessionService;
  private final LogoutService logoutService;
  private final RequestPasswordResetService requestPasswordResetService;
  private final ResetPasswordService resetPasswordService;
  private final AuthMapper mapper;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponseDto> register(
      @Valid @RequestBody RegisterRequestDto request) {
    var command = mapper.toCommand(request);
    var result = registerService.register(command);
    var response = mapper.toResponse(result);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(
      @Valid @RequestBody LoginRequestDto request, HttpServletRequest servletRequest) {
    var command =
        mapper.toCommand(
            request,
            servletRequest.getHeader("User-Agent"),
            parseAddress(servletRequest.getRemoteAddr()));
    var result = loginService.login(command);
    var response = mapper.toResponse(result);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/email/confirm")
  public ResponseEntity<Void> confirmEmail(@Valid @RequestBody ConfirmEmailRequestDto request) {
    confirmEmailService.confirm(request.token());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/email/resend")
  public ResponseEntity<Void> resendConfirmation(
      @Valid @RequestBody ResendConfirmationRequestDto request) {
    resendConfirmationService.resend(request.email());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponseDto> refresh(
      @Valid @RequestBody RefreshTokenRequestDto request, HttpServletRequest servletRequest) {
    var result =
        refreshSessionService.refresh(
            request.refreshToken(),
            servletRequest.getHeader("User-Agent"),
            parseAddress(servletRequest.getRemoteAddr()));
    return ResponseEntity.ok(
        new LoginResponseDto(
            "Bearer", result.accessToken(), result.refreshToken(), result.expiresInSeconds()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
    logoutService.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/password/forgot")
  public ResponseEntity<Void> requestPasswordReset(
      @Valid @RequestBody RequestPasswordResetDto request) {
    requestPasswordResetService.request(request.email());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/password/reset")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDto request) {
    resetPasswordService.reset(request.token(), request.newPassword());
    return ResponseEntity.noContent().build();
  }

  private InetAddress parseAddress(String address) {
    try {
      return InetAddress.getByName(address);
    } catch (UnknownHostException exception) {
      throw new IllegalArgumentException("Invalid client IP address", exception);
    }
  }
}
