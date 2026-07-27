package com.fitterapp.personal.dto.profile;
import java.util.List; import com.fitterapp.personal.entity.service.ServiceMode;
public record UpdateServiceModesRequestDto(List<ServiceMode> serviceModes) { }
