package com.fitterapp.storage;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
  String store(MultipartFile image, String namespace, UUID profileId);

  byte[] read(String key);

  void delete(String key);
}
