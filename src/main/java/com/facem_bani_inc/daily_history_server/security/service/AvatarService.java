package com.facem_bani_inc.daily_history_server.security.service;

import com.cloudinary.Cloudinary;
import com.facem_bani_inc.daily_history_server.model.EAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:daily-history}")
    private String folder;

    public String uploadFromUrl(String imageUrl, EAuthProvider provider, String providerUserId) {
        if (imageUrl == null || imageUrl.isBlank()) return null;

        String publicId = folder + "/avatars/" + provider.name().toLowerCase() + "/" + providerUserId;

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("public_id", publicId);
            options.put("overwrite", true);
            options.put("resource_type", "image");
            options.put("invalidate", true);

            Map<?, ?> result = cloudinary.uploader().upload(imageUrl, options);
            Object secureUrl = result.get("secure_url");
            return secureUrl != null ? secureUrl.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
