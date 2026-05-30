package com.facem_bani_inc.daily_history_server.security.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.facem_bani_inc.daily_history_server.model.enums.EAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public void deleteAvatar(EAuthProvider provider, String providerUserId) {
        if (provider == null || providerUserId == null || providerUserId.isBlank()) return;
        String publicId = folder + "/avatars/" + provider.name().toLowerCase() + "/" + providerUserId;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary avatar {}: {}", publicId, e.getMessage());
        }
    }
}
