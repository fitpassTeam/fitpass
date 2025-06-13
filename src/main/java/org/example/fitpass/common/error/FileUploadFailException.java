package org.example.fitpass.common.error;

import lombok.Getter;

@Getter
public class FileUploadFailException extends RuntimeException {
    private final String code;

    public FileUploadFailException(String message) {
        super(message);
        this.code = "FILE_UPLOAD_FAIL";
    }
}