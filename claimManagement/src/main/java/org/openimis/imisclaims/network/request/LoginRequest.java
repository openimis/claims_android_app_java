package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.network.dto.LoginDto;
import org.openimis.imisclaims.network.dto.TokenDto;

public class LoginRequest extends BaseFHIRPostRequest<LoginDto, TokenDto> {

    public LoginRequest() {
        super("login");
    }

    @NonNull
    @Override
    protected TokenDto fromJson(@NonNull JSONObject object) throws Exception {
        return TokenDto.fromJson(object);
    }

    @NonNull
    @Override
    protected JSONObject toJson(LoginDto object) throws JSONException {
        return new JSONObject()
                .put("username", object.getUsername())
                .put("password", object.getPassword());
    }
}
