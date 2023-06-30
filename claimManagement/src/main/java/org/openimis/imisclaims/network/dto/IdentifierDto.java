package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IdentifierDto {

    private static final String TYPE_CODE = "Code";

    @NonNull
    public static String getCode(@NonNull List<IdentifierDto> identifiers) {
        for (IdentifierDto identifier : identifiers) {
            if (identifier.getType().equals(TYPE_CODE)) {
                return identifier.getValue();
            }
        }
        throw new IllegalStateException("Couldn't find a value for '"+TYPE_CODE+"'");
    }

    @NonNull
    public static List<IdentifierDto> fromJson(@NonNull JSONArray array) throws JSONException {
        ArrayList<IdentifierDto> identifiers = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject identifier = array.getJSONObject(i);
            identifiers.add(
                    new IdentifierDto(
                            /* type = */ identifier.getJSONObject("type")
                            .getJSONArray("coding")
                            .getJSONObject(0)
                            .getString("code"),
                            /* value = */ identifier.getString("value")
                    )
            );
        }
        return identifiers;
    }

    @NonNull
    private final String type;
    @NonNull
    private final String value;

    public IdentifierDto(
            @NonNull String type,
            @NonNull String value
    ) {
        this.type = type;
        this.value = value;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @NonNull
    public String getValue() {
        return value;
    }
}
