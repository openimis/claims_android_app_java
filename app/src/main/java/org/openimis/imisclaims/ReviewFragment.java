package org.openimis.imisclaims;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Hiren on 06/09/2019.
 */

public class ReviewFragment extends Fragment {
    TextView health_facility_code;
    TextView healthFacilityName;
    TextView PatientName;
    TextView ClaimCode;
    TextView MainDg;
    TextView SecDg1;
    TextView SecDg4;
    TextView visit_date_from;
    TextView visit_date_to;
    TextView dateClaimed;
    TextView date_processed_from;
    TextView VisitType;
    TextView etGuaranteeNo;
    TextView Claimed;
    TextView Approved;
    TextView Adjusted;
    TextView ClaimStatus;
    TextView Explanation;
    TextView Adjustment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_review,container,false);


         health_facility_code = (TextView) v.findViewById(R.id.healthFacilityCode);
         healthFacilityName = (TextView) v.findViewById(R.id.healthFacilityName);
         PatientName = (TextView) v.findViewById(R.id.PatientName);
         ClaimCode = (TextView) v.findViewById(R.id.ClaimCode);
         MainDg = (TextView) v.findViewById(R.id.MainDg);
         SecDg1 = (TextView) v.findViewById(R.id.SecDg1);
         SecDg4 = (TextView) v.findViewById(R.id.SecDg4);
         visit_date_from = (TextView) v.findViewById(R.id.visit_date_from);
         visit_date_to = (TextView) v.findViewById(R.id.visit_date_to);
         dateClaimed = (TextView) v.findViewById(R.id.dateClaimed);
         date_processed_from = (TextView) v.findViewById(R.id.date_processed_from);
         VisitType = (TextView) v.findViewById(R.id.VisitType);
         etGuaranteeNo = (TextView) v.findViewById(R.id.etGuaranteeNo);
         Claimed = (TextView) v.findViewById(R.id.Claimed);
         Approved = (TextView) v.findViewById(R.id.Approved);
         Adjusted = (TextView) v.findViewById(R.id.Adjusted);
         ClaimStatus = (TextView) v.findViewById(R.id.ClaimStatus);
         Explanation = (TextView) v.findViewById(R.id.Explanation);
         Adjustment = (TextView) v.findViewById(R.id.Adjustment);


        health_facility_code.setText("sample");
        healthFacilityName.setText("sample");
        PatientName.setText("");
        ClaimCode.setText("");
        MainDg.setText("");
        SecDg1.setText("");
        SecDg4.setText("");
        visit_date_from.setText("");
        visit_date_to.setText("");
        dateClaimed.setText("");
        date_processed_from.setText("");
        VisitType.setText("");
        etGuaranteeNo.setText("");
        Claimed.setText("");
        Approved.setText("");
        Adjusted.setText("");
        ClaimStatus.setText("");
        Explanation.setText("");
        Adjustment.setText("");


        return v;
    }
}
