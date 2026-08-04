package com.fitterapp.personal.controller;

import com.fitterapp.personal.dto.modality.ModalityResponseDto;
import com.fitterapp.personal.service.modality.ListActiveModalitiesService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/modalities")
@RequiredArgsConstructor
public class ModalityController {

  private final ListActiveModalitiesService listActiveModalitiesService;

  @GetMapping
  public ResponseEntity<List<ModalityResponseDto>> list() {
    var response =
        listActiveModalitiesService.list().stream()
            .map(modality -> new ModalityResponseDto(modality.getId(), modality.getName(), modality.getSlug()))
            .toList();
    return ResponseEntity.ok(response);
  }
}
