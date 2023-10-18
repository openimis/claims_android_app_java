package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DiagnosisDto {

    @NonNull
    public static List<DiagnosisDto> fromJson(@NonNull JSONObject jsonObject) throws JSONException, ParseException {
        List<DiagnosisDto> diagnoses = new ArrayList<>();
        JSONArray jsonDiagnoses = jsonObject.getJSONArray("concept");
        for (int i = 0; i < jsonDiagnoses.length(); i++) {
            JSONObject diagnosis = jsonDiagnoses.getJSONObject(i);
            diagnoses.add(
                    new DiagnosisDto(
                            diagnosis.getString("code"),
                            diagnosis.getString("display")
                    )
            );
        }
        return diagnoses;
    }

    @NonNull
    private final String code;
    @NonNull
    private final String display;

    public DiagnosisDto(
            @NonNull String code,
            @NonNull String display
    ){
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
