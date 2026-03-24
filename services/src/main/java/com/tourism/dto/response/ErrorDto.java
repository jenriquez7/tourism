package com.tourism.dto.response;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public record ErrorDto (HttpStatus code, String message, String detail) implements Serializable {

    public static ErrorDto of(HttpStatus code, String message) {
        return new ErrorDto(code, message, null);
    }

    public static ErrorDto of(HttpStatus code, String message, String detail) {
        return new ErrorDto(code, message, detail);
    }
}
