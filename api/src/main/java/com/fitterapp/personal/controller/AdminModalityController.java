package com.fitterapp.personal.controller;

import com.fitterapp.personal.dto.admin.AdminModalityDto;
import com.fitterapp.personal.dto.modality.AdminModalityActivationRequestDto;
import com.fitterapp.personal.dto.modality.AdminModalityNameRequestDto;
import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.service.modality.AdminModalityService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/modalities")
@RequiredArgsConstructor
public class AdminModalityController {
  private final AdminModalityService service;

  @GetMapping
  public ResponseEntity<List<AdminModalityDto>> list() {
    return ResponseEntity.ok(service.list().stream().map(this::toResponse).toList());
  }

  @PostMapping
  public ResponseEntity<AdminModalityDto> create(
      @Valid @RequestBody AdminModalityNameRequestDto request) {
    AdminModalityDto response = toResponse(service.create(request.name()));
    return ResponseEntity.created(URI.create("/api/v1/admin/modalities/" + response.id()))
        .body(response);
  }

  @PutMapping("/{modalityId}")
  public ResponseEntity<AdminModalityDto> update(
      @PathVariable Short modalityId, @Valid @RequestBody AdminModalityNameRequestDto request) {
    return ResponseEntity.ok(toResponse(service.update(modalityId, request.name())));
  }

  @PatchMapping("/{modalityId}/activation")
  public ResponseEntity<AdminModalityDto> setActive(
      @PathVariable Short modalityId,
      @Valid @RequestBody AdminModalityActivationRequestDto request) {
    return ResponseEntity.ok(toResponse(service.setActive(modalityId, request.active())));
  }

  private AdminModalityDto toResponse(Modality modality) {
    return new AdminModalityDto(
        modality.getId(), modality.getName(), modality.getSlug(), modality.isActive());
  }
}
