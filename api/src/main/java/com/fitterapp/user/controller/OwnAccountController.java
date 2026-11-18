package com.fitterapp.user.controller;

import com.fitterapp.user.service.OwnAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class OwnAccountController {
  private final OwnAccountService service;

  @GetMapping
  public OwnAccountService.Account get(@AuthenticationPrincipal Jwt jwt) {
    return service.get(UUID.fromString(jwt.getSubject()));
  }

  @PutMapping
  public OwnAccountService.Account update(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateAccount request) {
    return service.update(
        UUID.fromString(jwt.getSubject()), request.fullName(), request.phoneNumber());
  }

  public record UpdateAccount(
      @NotBlank @Size(max = 120) String fullName,
      @NotBlank @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$") String phoneNumber) {}
}
