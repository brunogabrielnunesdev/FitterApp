package com.fitterapp.personal.dto.profile;

import com.fitterapp.personal.entity.service.ServiceMode;
import java.util.List;

public record UpdateServiceModesRequestDto(List<ServiceMode> serviceModes) {}
