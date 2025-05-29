package org.example.fitpass.common.error;

import org.springframework.http.HttpStatus;

public interface BaseCode {

    HttpStatus getHttpStatus();

    String getMessage();

    int getBaseCode();
}
