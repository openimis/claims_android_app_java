package org.openimis.imisclaims.network.dto;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MedicationDto {

    @NonNull
    public static List<MedicationDto> fromJson(@NonNull JSONObject jsonObject) throws JSONException, ParseException {
        ArrayList<MedicationDto> list = new ArrayList<>();
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
                    new MedicationDto(
                            /* id = */ entry.getString("id"),
                            /* identifiers = */ IdentifierDto.fromJson(entry.getJSONArray("identifier")),
                            /* title = */ entry.getJSONObject("code").getString("text"),
                            /* price = */ value.getDouble("value"),
                            /* currency = */ value.getString("currency"),
                            /* status = */ entry.getString("status"),
                            /* amount = */ entry.has("amount") ?
                            entry.getJSONObject("amount").getJSONObject("numerator").getDouble("value")
                            : -1.0
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
    private final String title;
    private final double price;
    @NonNull
    private final String currency;
    @NonNull
    private final String status;
    private final double amount;

    public MedicationDto(@NonNull String id, @NonNull List<IdentifierDto> identifiers, @NonNull String title, double price, @NonNull String currency, @NonNull String status, double amount) {
        this.id = id;
        this.identifiers = identifiers;
        this.title = title;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.amount = amount;
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

    public double getAmount() {
        return amount;
    }
}
