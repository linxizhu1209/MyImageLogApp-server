package com.imglog.myimagelogserver.security.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.encryption")
public record EncryptionProperties(
        boolean enabled,
        String keyBase64
) {
}
