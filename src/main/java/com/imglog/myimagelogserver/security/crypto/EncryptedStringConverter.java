package com.imglog.myimagelogserver.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return EncryptionRegistry.get().encryptText(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return EncryptionRegistry.get().decryptText(dbData);
    }
}
