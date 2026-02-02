package com.imglog.myimagelogserver.image.storage;

import com.imglog.myimagelogserver.image.service.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StoragePort {
    StoredFile store(MultipartFile file) throws IOException;

}
