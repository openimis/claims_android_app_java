package org.openimis.imisclaims;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.util.TextViewUtils;


/**
 * Created by Hiren on 06/09/2019.
 */

public class ReviewFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Claim claim = ((ClaimReview) getContext()).claim;

        TextView health_facility_code = v.findViewById(R.id.healthFacilityCode);
        TextView healthFacilityName = v.findViewById(R.id.healthFacilityName);
        TextView insuranceNumber = v.findViewById(R.id.insuranceNo);
        TextView PatientName = v.findViewById(R.id.PatientName);
        TextView ClaimCode = v.findViewById(R.id.ClaimCode);
        TextView MainDg = v.findViewById(R.id.MainDg);
        TextView SecDg1 = v.findViewById(R.id.SecDg1);
        TextView SecDg2 = v.findViewById(R.id.SecDg2);
        TextView SecDg3 = v.findViewById(R.id.SecDg3);
        TextView SecDg4 = v.findViewById(R.id.SecDg4);
        TextView visit_date_from = v.findViewById(R.id.visit_date_from);
        TextView visit_date_to = v.findViewById(R.id.visit_date_to);
        TextView dateClaimed = v.findViewById(R.id.dateClaimed);
        TextView date_processed_from = v.findViewById(R.id.date_processed_from);
        TextView VisitType = v.findViewById(R.id.VisitType);
        TextView etGuaranteeNo = v.findViewById(R.id.etGuaranteeNo);
        TextView Claimed = v.findViewById(R.id.Claimed);
        TextView Approved = v.findViewById(R.id.Approved);
        TextView Adjusted = v.findViewById(R.id.Adjusted);
        TextView ClaimStatus = v.findViewById(R.id.ClaimStatus);
        TextView Explanation = v.findViewById(R.id.Explanation);
        TextView Adjustment = v.findViewById(R.id.Adjustment);


        health_facility_code.setText(claim.getHealthFacilityCode());
        healthFacilityName.setText(claim.getHealthFacilityName());
        insuranceNumber.setText(claim.getInsuranceNumber());
        PatientName.setText(claim.getPatientName());
        ClaimCode.setText(claim.getClaimNumber());
        MainDg.setText(claim.getMainDg());
        SecDg1.setText(claim.getSecDg1());
        SecDg2.setText(claim.getSecDg2());
        SecDg3.setText(claim.getSecDg3());
        SecDg4.setText(claim.getSecDg4());
        TextViewUtils.setDate(visit_date_from, claim.getVisitDateFrom());
        TextViewUtils.setDate(visit_date_to, claim.getVisitDateTo());
        TextViewUtils.setDate(dateClaimed, claim.getDateClaimed());
        TextViewUtils.setDate(date_processed_from, claim.getVisitDateTo());
        VisitType.setText(claim.getVisitType());
        etGuaranteeNo.setText(claim.getGuaranteeNumber());
        Claimed.setText(String.valueOf(claim.getClaimed()));
        Approved.setText(String.valueOf(claim.getApproved()));
        Adjusted.setText(String.valueOf(claim.getAdjusted()));
        ClaimStatus.setText(claim.getStatus() != null ? claim.getStatus().name() : null);
        Explanation.setText(claim.getExplanation());
        Adjustment.setText(claim.getAdjustment());
    }
}
