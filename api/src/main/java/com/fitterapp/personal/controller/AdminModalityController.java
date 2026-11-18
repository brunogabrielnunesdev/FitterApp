package com.fitterapp.personal.controller;

import com.fitterapp.personal.service.modality.AdminModalityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/modalities")
@RequiredArgsConstructor
public class AdminModalityController {
  private final AdminModalityService service;

  @GetMapping
  public List<AdminModalityService.View> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminModalityService.View create(@Valid @RequestBody Name request) {
    return service.create(request.name());
  }

  @PutMapping("/{id}")
  public AdminModalityService.View update(
      @PathVariable Short id, @Valid @RequestBody Name request) {
    return service.update(id, request.name());
  }

  @PatchMapping("/{id}/activation")
  public AdminModalityService.View activate(
      @PathVariable Short id, @Valid @RequestBody Activation request) {
    return service.setActive(id, request.active());
  }

  public record Name(@NotBlank @Size(max = 80) String name) {}

  public record Activation(@jakarta.validation.constraints.NotNull Boolean active) {}
}
