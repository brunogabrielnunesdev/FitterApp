package com.fitterapp.personal.service.contact;

import java.util.UUID;

public record StartWhatsappContactCommand(String slug, UUID viewerId) {}
