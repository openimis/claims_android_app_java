package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;

public class TokenDto {

    @NonNull
    private final String token;
    @NonNull
    private final long expiresOn;

    public TokenDto(
            @NonNull String token,
            @NonNull long expiresOn
    ){
        this.token = token;
        this.expiresOn = expiresOn;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    @NonNull
    public long getExpiresOn() {
        return expiresOn;
    }
}
