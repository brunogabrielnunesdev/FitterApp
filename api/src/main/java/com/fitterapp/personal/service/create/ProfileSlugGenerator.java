package com.fitterapp.personal.service.create;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ProfileSlugGenerator {

    private static final int MAX_LENGTH = 150;

    public String generate(String fullName, UUID userId) {
        String normalized = Normalizer.normalize(fullName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");

        if (normalized.isBlank() || !Character.isLetter(normalized.charAt(0))) {
            normalized = "personal";
        }

        String suffix = "-" + userId.toString().substring(0, 8);
        int baseMaxLength = MAX_LENGTH - suffix.length();
        String base = normalized.substring(0, Math.min(normalized.length(), baseMaxLength));
        return base.replaceAll("-+$", "") + suffix;
    }
}
