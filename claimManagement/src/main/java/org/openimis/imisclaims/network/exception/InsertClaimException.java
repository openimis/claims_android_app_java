package org.openimis.imisclaims.network.exception;

import androidx.annotation.NonNull;

public class InsertClaimException extends RuntimeException {

    @NonNull
    private final String severity;
    @NonNull
    private final String code;

    public InsertClaimException(
            @NonNull String severity,
            @NonNull String code,
            @NonNull String message
    ) {
        super(message);
        this.severity = severity;
        this.code = code;
    }

    @NonNull
    public String getSeverity() {
        return severity;
    }

    @NonNull
    public String getCode() {
        return code;
    }
}
