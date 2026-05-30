package com.imglog.myimagelogserver.security.crypto;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 필드/파일 암호화. DB 값은 {@code ENC$} 접두사 + Base64(IV||ciphertext).
 */
@Service
public class EncryptionService {

    private static final String PREFIX = "ENC$";
    private static final String FILE_MAGIC = "MIL1";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final boolean enabled;
    private final SecretKey aesKey;
    private final SecretKey hmacKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(EncryptionProperties properties) {
        this.enabled = properties.enabled();
        byte[] keyMaterial = decodeKey(properties.keyBase64());
        this.aesKey = new SecretKeySpec(keyMaterial, "AES");
        this.hmacKey = new SecretKeySpec(deriveHmacKey(keyMaterial), HMAC_SHA256);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String encryptText(String plain) {
        if (!enabled || plain == null || plain.isBlank()) {
            return plain;
        }
        if (plain.startsWith(PREFIX)) {
            return plain;
        }
        return PREFIX + Base64.getEncoder().encodeToString(encryptBytes(plain.getBytes(StandardCharsets.UTF_8)));
    }

    public String decryptText(String stored) {
        if (stored == null || stored.isBlank()) {
            return stored;
        }
        if (!stored.startsWith(PREFIX)) {
            return stored;
        }
        if (!enabled) {
            throw new IllegalStateException("암호화된 데이터가 있으나 app.encryption.enabled=false 입니다.");
        }
        byte[] payload = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
        return new String(decryptBytes(payload), StandardCharsets.UTF_8);
    }

    public byte[] encryptFile(byte[] plain) {
        if (!enabled) {
            return plain;
        }
        byte[] cipher = encryptBytes(plain);
        ByteBuffer buf = ByteBuffer.allocate(FILE_MAGIC.length() + cipher.length);
        buf.put(FILE_MAGIC.getBytes(StandardCharsets.US_ASCII));
        buf.put(cipher);
        return buf.array();
    }

    public byte[] decryptFile(byte[] stored) {
        if (stored == null || stored.length == 0) {
            return stored;
        }
        if (!startsWithMagic(stored)) {
            return stored;
        }
        if (!enabled) {
            throw new IllegalStateException("암호화된 파일이 있으나 app.encryption.enabled=false 입니다.");
        }
        byte[] cipher = new byte[stored.length - FILE_MAGIC.length()];
        System.arraycopy(stored, FILE_MAGIC.length(), cipher, 0, cipher.length);
        return decryptBytes(cipher);
    }

    public boolean isEncryptedFile(byte[] stored) {
        return stored != null && startsWithMagic(stored);
    }

    public String oauthLookupKey(String provider, String oauthId) {
        String raw = provider + ":" + oauthId;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(hmacKey);
            byte[] digest = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("oauth lookup key 생성 실패", e);
        }
    }

    private byte[] encryptBytes(byte[] plain) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plain);
            ByteBuffer buf = ByteBuffer.allocate(iv.length + encrypted.length);
            buf.put(iv);
            buf.put(encrypted);
            return buf.array();
        } catch (Exception e) {
            throw new IllegalStateException("암호화 실패", e);
        }
    }

    private byte[] decryptBytes(byte[] payload) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(payload);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buf.get(iv);
            byte[] encrypted = new byte[buf.remaining()];
            buf.get(encrypted);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("복호화 실패", e);
        }
    }

    private static byte[] decodeKey(String keyBase64) {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException(
                    "app.encryption.key-base64 가 필요합니다. (32바이트 Base64, 환경변수 APP_ENCRYPTION_KEY)"
            );
        }
        byte[] key = Base64.getDecoder().decode(keyBase64.trim());
        if (key.length != 32) {
            throw new IllegalStateException("암호화 키는 32바이트(AES-256)여야 합니다. 현재=" + key.length);
        }
        return key;
    }

    private static byte[] deriveHmacKey(byte[] master) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update("myimagelog-oauth-hmac".getBytes(StandardCharsets.UTF_8));
            digest.update(master);
            return digest.digest();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean startsWithMagic(byte[] data) {
        byte[] magic = FILE_MAGIC.getBytes(StandardCharsets.US_ASCII);
        if (data.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
