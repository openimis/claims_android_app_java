package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONObject;
import org.openimis.imisclaims.network.dto.PractitionerDto;
import org.openimis.imisclaims.network.response.PaginatedResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetPractitionersRequest extends BaseFHIRGetPaginatedRequest<PractitionerDto> {

    public GetPractitionersRequest() {
        super("Practitioner");
    }

    @WorkerThread
    public PaginatedResponse<PractitionerDto> get(int page, boolean onlyClaimAdmins) throws Exception {
        Map<String, String> parameters = new HashMap<>();
        if (onlyClaimAdmins) {
            parameters.put("resourceType", "ca");
        }
        return get(parameters, page);
    }

    @NonNull
    @Override
    protected List<PractitionerDto> getValueFromJson(@NonNull JSONObject object) throws Exception {
        return PractitionerDto.fromJson(object);
    }
}
