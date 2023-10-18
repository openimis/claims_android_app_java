package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.util.DateUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class PractitionerDto {

    @NonNull
    public static List<PractitionerDto> fromJson(@NonNull JSONObject jsonObject) throws JSONException, ParseException {
        ArrayList<PractitionerDto> list = new ArrayList<>();
        JSONArray entries = jsonObject.getJSONArray("entry");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i).getJSONObject("resource");
            List<CodeDto> qualifications = new ArrayList<>();
            JSONArray jsonQualifications = entry.getJSONArray("qualification");
            for (int j = 0; j < jsonQualifications.length(); j++) {
                qualifications.add(CodeDto.fromJson(jsonQualifications.getJSONObject(j).getJSONObject("code")));
            }
            String healthFacilityCode = null;
            if (entry.has("extension")) {
                JSONArray extensions = entry.getJSONArray("extension");
                for (int k = 0; k < extensions.length() ; k++) {
                    JSONObject extension = extensions.getJSONObject(k);
                    if (extension.has("valueReference")) {
                        JSONObject value = extension.getJSONObject("valueReference");
                        if (value.has("type") && "Organization".equals(value.getString("type"))) {
                            healthFacilityCode = value.getJSONObject("identifier").getString("value");
                            break;
                        }
                    }
                }
            }
            Date birthDate = null;
            if (entry.has("birthDate")) {
                birthDate = DateUtils.dateFromString(entry.getString("birthDate"));
            }
            list.add(
                    new PractitionerDto(
                            /* id = */ entry.getString("id"),
                            /* identifiers = */ IdentifierDto.fromJson(entry.getJSONArray("identifier")),
                            /* birthDate = */ birthDate,
                            /* names = */ Name.fromJson(entry.getJSONArray("name")),
                            /* qualifications = */ qualifications,
                            /* healthFacilityCode = */ healthFacilityCode
                    )
            );
        }
        return list;
    }

    @NonNull
    private final String id;
    @NonNull
    private final List<IdentifierDto> identifiers;
    @Nullable
    private final Date birthDate;
    @NonNull
    private final List<Name> names;
    @NonNull
    private final List<CodeDto> qualifications;
    @Nullable
    private final String healthFacilityCode;

    public PractitionerDto(
            @NonNull String id,
            @NonNull List<IdentifierDto> identifiers,
            @Nullable Date birthDate,
            @NonNull List<Name> names,
            @NonNull List<CodeDto> qualifications,
            @Nullable String healthFacilityCode
    ) {
        this.id = id;
        this.identifiers = identifiers;
        this.birthDate = birthDate;
        this.names = names;
        this.qualifications = qualifications;
        this.healthFacilityCode = healthFacilityCode;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public List<IdentifierDto> getIdentifiers() {
        return identifiers;
    }

    @Nullable
    public Date getBirthDate() {
        return birthDate;
    }

    @NonNull
    public List<Name> getNames() {
        return names;
    }

    @NonNull
    public List<CodeDto> getQualifications() {
        return qualifications;
    }

    @Nullable
    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public static class Name {

        @NonNull
        private static List<Name> fromJson(@NonNull JSONArray array) throws JSONException {
            ArrayList<Name> names = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject name = array.getJSONObject(i);
                ArrayList<String> given = new ArrayList<>();
                JSONArray jsonGiven = name.getJSONArray("given");
                for (int j = 0; j < jsonGiven.length(); j++) {
                    given.add(jsonGiven.getString(j));
                }
                names.add(
                        new Name(
                                /* use = */ name.getString("use"),
                                /* family = */ name.getString("family"),
                                /* given = */ given
                        )
                );
            }
            return names;
        }

        @NonNull
        private final String use;
        @NonNull
        private final String family;
        @NonNull
        private final List<String> given;

        public Name(
                @NonNull String use,
                @NonNull String family,
                @NonNull List<String> given
        ) {

            this.use = use;
            this.family = family;
            this.given = given;
        }

        @NonNull
        public String getUse() {
            return use;
        }

        @NonNull
        public String getFamily() {
            return family;
        }

        @NonNull
        public List<String> getGiven() {
            return given;
        }
    }
}
