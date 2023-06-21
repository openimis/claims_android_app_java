package org.openimis.imisclaims.network.exception;

import androidx.annotation.Nullable;

public class HttpException extends RuntimeException {

    private final int code;

    public HttpException(
            int code,
            @Nullable String message,
            @Nullable Throwable cause
    ) {
        super("HTTP " + code + " - " + message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
