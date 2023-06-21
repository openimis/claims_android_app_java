package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.openimis.imisclaims.network.dto.DiagnosisDto;

import java.util.List;

public class GetDiagnosesRequest extends BaseFHIRGetRequest<List<DiagnosisDto>> {

    public GetDiagnosesRequest() {
        super("CodeSystem/diagnosis");
    }

    @NonNull
    @Override
    protected List<DiagnosisDto> fromJson(@NonNull JSONObject object) throws Exception {
        return DiagnosisDto.fromJson(object);
    }
}
