package com.fitterapp.personal.service.admin;

import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.service.service.ServiceAreaInput;
import java.util.List;

public record AdminPersonalInput(
    String fullName,
    String biography,
    String whatsapp,
    Short experienceStartedYear,
    String certifications,
    String gymsDescription,
    Integer startingPriceCents,
    PriceUnit priceUnit,
    List<Short> modalityIds,
    List<ServiceMode> serviceModes,
    List<ServiceAreaInput> serviceAreas,
    String crefRegistrationCode,
    String crefDocumentImageKey) {}
