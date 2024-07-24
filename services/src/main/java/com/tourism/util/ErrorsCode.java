package com.tourism.util;

import lombok.Getter;

@Getter
public enum ErrorsCode {

    GENERIC(null, null),
    LOGIN_FAILED("Login has failed", null),
    LOGOUT_FAILED("logout has failed", null),
    INVALID_REFRESH_TOKEN("Invalid refresh token", null);


    private String message;

    private String customMessage;

    ErrorsCode(String message, String customMessage) {
        this.setMessage(message);
        this.setCustomMessage(customMessage);
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }
}
