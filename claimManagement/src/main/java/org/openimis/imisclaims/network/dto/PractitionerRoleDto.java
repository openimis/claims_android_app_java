package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class PractitionerRoleDto {

    @NonNull
    public static List<PractitionerRoleDto> fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        ArrayList<PractitionerRoleDto> list = new ArrayList<>();
        JSONArray entries = jsonObject.getJSONArray("entry");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i).getJSONObject("resource");
            list.add(
                    new PractitionerRoleDto(
                            /* id = */ entry.getString("id"),
                            /* identifiers = */ IdentifierDto.fromJson(entry.getJSONArray("identifier")),
                            /* practitionerIdentifiers = */ IdentifierDto.fromJson(entry.getJSONObject("practitioner").getJSONArray("identifier")),
                            /* organizationIdentifiers = */ IdentifierDto.fromJson(entry.getJSONObject("organization").getJSONArray("identifier")),
                            /* code = */ CodeDto.fromJson(entry.getJSONArray("code"))

                    )
            );
        }
        return list;
    }

    @NonNull
    private final String id;
    @NonNull
    private final List<IdentifierDto> identifiers;
    @NonNull
    private final List<IdentifierDto> practitionerIdentifiers;
    @NonNull
    private final List<IdentifierDto> organizationIdentifiers;
    @NonNull
    private final List<CodeDto> codes;

    public PractitionerRoleDto(
            @NonNull String id,
            @NonNull List<IdentifierDto> identifiers,
            @NonNull List<IdentifierDto> practitionerIdentifiers,
            @NonNull List<IdentifierDto> organizationIdentifiers,
            @NonNull List<CodeDto> codes
    ) {
        this.id = id;
        this.identifiers = identifiers;
        this.practitionerIdentifiers = practitionerIdentifiers;
        this.organizationIdentifiers = organizationIdentifiers;
        this.codes = codes;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public List<IdentifierDto> getIdentifiers() {
        return identifiers;
    }

    @NonNull
    public List<IdentifierDto> getPractitionerIdentifiers() {
        return practitionerIdentifiers;
    }

    @NonNull
    public List<IdentifierDto> getOrganizationIdentifiers() {
        return organizationIdentifiers;
    }

    @NonNull
    public List<CodeDto> getCodes() {
        return codes;
    }
}
