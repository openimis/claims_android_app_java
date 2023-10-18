package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.openimis.imisclaims.network.dto.ActivityDefinitionDto;

import java.util.List;

public class GetActivityDefinitionsRequest extends BaseFHIRGetPaginatedRequest<ActivityDefinitionDto> {

    public GetActivityDefinitionsRequest() {
        super("ActivityDefinition");
    }

    @Override
    @NonNull
    protected List<ActivityDefinitionDto> getValueFromJson(@NonNull JSONObject object) throws Exception {
        return ActivityDefinitionDto.fromJson(object);
    }
}
