package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CodeDto {

    @NonNull
    public static List<CodeDto> fromJson(@NonNull JSONArray array) throws JSONException {
        ArrayList<CodeDto> codes = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            codes.add(fromJson(array.getJSONObject(i)));
        }
        return codes;
    }

    @NonNull
    public static CodeDto fromJson(@NonNull JSONObject object) throws JSONException {
        JSONObject code = object.getJSONArray("coding").getJSONObject(0);
        return new CodeDto(
                /* code = */ code.getString("code"),
                /* display = */ code.getString("display")
        );
    }

    @NonNull
    private final String code;
    @NonNull
    private final String display;

    public CodeDto(
            @NonNull String code,
            @NonNull String display
    ) {
        this.code = code;
        this.display = display;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getDisplay() {
        return display;
    }
}
