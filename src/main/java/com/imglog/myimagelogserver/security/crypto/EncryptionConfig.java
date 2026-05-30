package com.imglog.myimagelogserver.security.crypto;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EncryptionProperties.class)
public class EncryptionConfig {
}
