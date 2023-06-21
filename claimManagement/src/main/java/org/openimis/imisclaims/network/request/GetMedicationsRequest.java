package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.openimis.imisclaims.network.dto.MedicationDto;

import java.util.List;

public class GetMedicationsRequest extends BaseFHIRGetPaginatedRequest<MedicationDto> {

    public GetMedicationsRequest() {
        super("Medication");
    }

    @NonNull
    @Override
    protected List<MedicationDto> getValueFromJson(@NonNull JSONObject object) throws Exception {
        return MedicationDto.fromJson(object);
    }
}
