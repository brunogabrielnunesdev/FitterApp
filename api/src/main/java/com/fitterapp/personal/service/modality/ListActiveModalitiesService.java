package com.fitterapp.personal.service.modality;

import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.repository.ModalityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListActiveModalitiesService {

  private final ModalityRepository modalities;

  @Transactional(readOnly = true)
  public List<Modality> list() {
    return modalities.findAllByActiveTrueOrderByNameAsc();
  }
}
