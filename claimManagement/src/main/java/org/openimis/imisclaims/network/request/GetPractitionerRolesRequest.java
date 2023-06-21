package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.openimis.imisclaims.network.dto.PractitionerRoleDto;

import java.util.List;

public class GetPractitionerRolesRequest extends BaseFHIRGetPaginatedRequest<PractitionerRoleDto> {

    GetPractitionerRolesRequest() {
        super("PractitionerRole");
    }

    @NonNull
    @Override
    protected List<PractitionerRoleDto> getValueFromJson(@NonNull JSONObject object) throws Exception {
        return PractitionerRoleDto.fromJson(object);
    }
}
