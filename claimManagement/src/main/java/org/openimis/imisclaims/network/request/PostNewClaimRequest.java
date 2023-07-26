package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.domain.entity.PendingClaim;
import org.openimis.imisclaims.util.DateUtils;

import java.util.Objects;

public class PostNewClaimRequest extends BaseFHIRPostRequest<PendingClaim, Boolean> {

    public PostNewClaimRequest() {
        super("Claim");
    }

    @NonNull
    @Override
    protected Boolean fromJson(@NonNull JSONObject object) throws Exception {
        String resourceType = object.getString("resourceType");
        if ("ClaimResponse".equals(resourceType)) {
            return object.has("total");
        }
        throw new IllegalStateException("ResourceType '"+resourceType+"' is unknown");
    }

    @NonNull
    @Override
    protected JSONObject toJson(PendingClaim object) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resourceType", "Claim");
        JSONObject billablePeriod = new JSONObject();
        billablePeriod.put("start", DateUtils.toDateString(object.getStartDate()));
        billablePeriod.put("end", DateUtils.toDateString(object.getEndDate()));
        jsonObject.put("billablePeriod", billablePeriod);
        jsonObject.put("created", DateUtils.toDateString(object.getClaimDate()));
        JSONObject type = new JSONObject();
        JSONObject coding = new JSONObject();
        coding.put("system", "https://openimis.github.io/openimis_fhir_r4_ig/CodeSystem-claim-visit-type.html");
        coding.put("code", object.getVisitType());
        coding.put("display", getVisitTypeCode(object.getVisitType()));
        type.put("coding", wrapInArray(coding));
        jsonObject.put("type", type);
        JSONObject identifier = new JSONObject();
        JSONObject typeIdentifier = new JSONObject();
        JSONArray typeIdentifierCodings = new JSONArray();
        JSONObject typeIdentifierCoding = new JSONObject();
        typeIdentifierCoding.put("system", "https://openimis.github.io/openimis_fhir_r4_ig/CodeSystem/openimis-identifiers");
        typeIdentifierCoding.put("code", "Code");
        typeIdentifierCodings.put(typeIdentifierCoding);
        typeIdentifier.put("coding", typeIdentifierCodings);
        identifier.put("type", typeIdentifier);
        identifier.put("value", object.getClaimCode());
        jsonObject.put("identifier", wrapInArray(identifier));
        JSONArray diagnoses = new JSONArray();
        int sequence = 1;
        diagnoses.put(getDiagnosisJson(object.getIcdCode(), sequence++));
        if (object.getIcdCode1() != null && !Objects.equals(object.getIcdCode1(), "")) {
            diagnoses.put(getDiagnosisJson(object.getIcdCode1(), sequence++));
        }
        if (object.getIcdCode2() != null && !Objects.equals(object.getIcdCode2(), "")) {
            diagnoses.put(getDiagnosisJson(object.getIcdCode2(), sequence++));
        }
        if (object.getIcdCode3() != null && !Objects.equals(object.getIcdCode3(), "")) {
            diagnoses.put(getDiagnosisJson(object.getIcdCode3(), sequence++));
        }
        if (object.getIcdCode4() != null && !Objects.equals(object.getIcdCode4(), "")) {
            diagnoses.put(getDiagnosisJson(object.getIcdCode4(), sequence));
        }
        jsonObject.put("diagnosis", diagnoses);
        if (object.getClaimAdminCode() != null && !Objects.equals(object.getClaimAdminCode(), "")) {
            jsonObject.put("enterer", code("Practitioner", object.getClaimAdminCode()));
        }
        if (object.getGuaranteeNumber() != null && !Objects.equals(object.getGuaranteeNumber(), "")) {
            JSONObject insurance = new JSONObject();
            insurance.put("coverage", code("Coverage", object.getGuaranteeNumber()));
            insurance.put( "focal",true);
            insurance.put( "sequence", 1);
            jsonObject.put("insurance",  wrapInArray(insurance));
        }
        JSONArray items = new JSONArray();
        sequence = 1;
        double total = 0;
        for (PendingClaim.Medication medication : object.getMedications()) {
            items.put(toJson(medication, sequence++));
            total += medication.getPrice();
        }
        for (PendingClaim.Service service : object.getServices()) {
            items.put(toJson(service, sequence++));
            total += service.getPrice();
        }
        jsonObject.put("item", items);
        jsonObject.put("patient", code("Patient", object.getChfId()));
        JSONObject priority = new JSONObject();
        JSONObject priorityCoding = new JSONObject();
        priorityCoding.put("code", "normal");
        priority.put("coding", wrapInArray(priorityCoding));
        jsonObject.put("priority", priority);
        jsonObject.put("provider", code("Organization", object.getHealthFacilityCode()));
        jsonObject.put("status", "active");
        JSONObject totalJson = new JSONObject();
        totalJson.put("currency", "$");
        totalJson.put("value", total);
        jsonObject.put("total", totalJson);
        jsonObject.put("use", "claim");
        return jsonObject;
    }

    @NonNull
    private String getVisitTypeCode(@NonNull String type) {
        switch (type) {
            case "E":
                return "Emergency";
            case "R":
                return "Referral";
            case "O":
                return "Other";
            default:
                throw new IllegalArgumentException("Type '" + type + "' is unknown");
        }
    }

    @NonNull
    private JSONObject getDiagnosisJson(@NonNull String code, int sequence) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("sequence", sequence);
        JSONObject diagnosis = new JSONObject();
        JSONObject coding = new JSONObject();
        coding.put("system", "https://openimis.github.io/openimis_fhir_r4_ig/CodeSystem/diagnosis-ICD10-level1");
        coding.put("code", code);
        diagnosis.put("coding", wrapInArray(coding));
        object.put("diagnosisCodeableConcept", diagnosis);
        return object;
    }

    @NonNull
    private JSONObject toJson(@NonNull PendingClaim.Medication medication, int sequence) throws JSONException {
        JSONObject object = new JSONObject();
        JSONObject category = new JSONObject();
        category.put("text", "item");
        object.put("category", category);
        JSONObject extension = new JSONObject();
        extension.put("url", "Medication");
        extension.put("valueReference", code("Medication", medication.getCode()));
        object.put("extension", wrapInArray(extension));
        JSONObject productOrService = new JSONObject();
        productOrService.put("text", medication.getCode());
        object.put("productOrService", productOrService);
        JSONObject quantity = new JSONObject();
        quantity.put("value", medication.getQuantity());
        object.put("quantity", quantity);
        object.put("sequence", sequence);
        JSONObject unitPrice = new JSONObject();
        unitPrice.put("currency", "$");
        unitPrice.put("value", medication.getPrice());
        object.put("unitPrice", unitPrice);
        return object;
    }

    @NonNull
    private JSONObject toJson(@NonNull PendingClaim.Service service, int sequence) throws JSONException {
        JSONObject object = new JSONObject();
        JSONObject category = new JSONObject();
        category.put("text", "service");
        object.put("category", category);
        JSONObject extension = new JSONObject();
        extension.put("url", "ActivityDefinition");
        extension.put("valueReference", code("ActivityDefinition", service.getCode()));
        object.put("extension", wrapInArray(extension));
        JSONObject productOrService = new JSONObject();
        productOrService.put("text", service.getCode());
        object.put("productOrService", productOrService);
        JSONObject quantity = new JSONObject();
        quantity.put("value", service.getQuantity());
        object.put("quantity", quantity);
        object.put("sequence", sequence);
        JSONObject unitPrice = new JSONObject();
        unitPrice.put("currency", "$");
        unitPrice.put("value", service.getPrice());
        object.put("unitPrice", unitPrice);
        return object;
    }

    @NonNull
    private JSONArray wrapInArray(@NonNull JSONObject jsonObject){
        JSONArray array = new JSONArray();
        array.put(jsonObject);
        return array;
    }

    @NonNull
    private JSONObject code(@NonNull String type, @NonNull String code) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type);
        JSONObject identifier = new JSONObject();
        identifier.put("value", code);
        JSONObject jsonType = new JSONObject();
        JSONObject coding = new JSONObject();
        coding.put("system", "https://openimis.github.io/openimis_fhir_r4_ig/CodeSystem/openimis-identifiers");
        coding.put("code", "Code");
        jsonType.put("coding", wrapInArray(coding));
        identifier.put("type", jsonType);
        json.put("identifier", identifier);
        return json;
    }
}
