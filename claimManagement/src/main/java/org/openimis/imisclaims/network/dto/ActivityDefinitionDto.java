package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ActivityDefinitionDto {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

    @NonNull
    public static List<ActivityDefinitionDto> fromJson(@NonNull JSONObject jsonObject) throws JSONException, ParseException {
        ArrayList<ActivityDefinitionDto> list = new ArrayList<>();
        JSONArray entries = jsonObject.getJSONArray("entry");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i).getJSONObject("resource");
            JSONArray extensions = entry.getJSONArray("extension");
            JSONObject value = null;
            for (int j = 0; j < extensions.length(); j++) {
                JSONObject extension = extensions.getJSONObject(j);
                if (extension.has("valueMoney")) {
                    value = extension.getJSONObject("valueMoney");
                    break;
                }
            }
            Objects.requireNonNull(value);
            list.add(
                    new ActivityDefinitionDto(
                            /* id = */ entry.getString("id"),
                            /* identifiers = */ IdentifierDto.fromJson(entry.getJSONArray("identifier")),
                            /* name = */ entry.getString("name"),
                            /* title = */ entry.getString("title"),
                            /* price = */ value.getDouble("value"),
                            /* currency = */ value.getString("currency"),
                            /* status = */ entry.getString("status"),
                            /* date = */ Objects.requireNonNull(DATE_FORMAT.parse(entry.getString("date")))
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
    private final String name;
    @NonNull
    private final String title;
    private final double price;
    @NonNull
    private final String currency;
    @NonNull
    private final String status;
    @NonNull
    private final Date date;

    public ActivityDefinitionDto(
            @NonNull String id,
            @NonNull List<IdentifierDto> identifiers,
            @NonNull String name,
            @NonNull String title,
            double price,
            @NonNull String currency,
            @NonNull String status,
            @NonNull Date date
    ){
        this.id = id;
        this.identifiers = identifiers;
        this.name = name;
        this.title = title;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.date = date;
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
    public String getName() {
        return name;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    @NonNull
    public String getCurrency() {
        return currency;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    @NonNull
    public Date getDate() {
        return date;
    }
}
