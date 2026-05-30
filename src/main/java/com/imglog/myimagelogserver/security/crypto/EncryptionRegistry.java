package com.imglog.myimagelogserver.security.crypto;

/**
 * JPA {@link EncryptedStringConverter}에서 Spring DI 없이 {@link EncryptionService}에 접근하기 위한 홀더.
 */
final class EncryptionRegistry {

    private static volatile EncryptionService service;

    private EncryptionRegistry() {}

    static void register(EncryptionService encryptionService) {
        service = encryptionService;
    }

    static EncryptionService get() {
        EncryptionService current = service;
        if (current == null) {
            throw new IllegalStateException("EncryptionService가 아직 초기화되지 않았습니다.");
        }
        return current;
    }
}
