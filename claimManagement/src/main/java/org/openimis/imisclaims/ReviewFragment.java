package org.openimis.imisclaims;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

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
    TextView SecDg2;
    TextView SecDg3;
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


        String c = ((ClaimReview)getContext()).claims;

        try {

                JSONObject object = new JSONObject(c);

                health_facility_code = (TextView) v.findViewById(R.id.healthFacilityCode);
                healthFacilityName = (TextView) v.findViewById(R.id.healthFacilityName);
                PatientName = (TextView) v.findViewById(R.id.PatientName);
                ClaimCode = (TextView) v.findViewById(R.id.ClaimCode);
                MainDg = (TextView) v.findViewById(R.id.MainDg);
                SecDg1 = (TextView) v.findViewById(R.id.SecDg1);
                SecDg2 = (TextView) v.findViewById(R.id.SecDg2);
                SecDg3 = (TextView) v.findViewById(R.id.SecDg3);
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


                health_facility_code.setText(object.getString("health_facility_code"));
                healthFacilityName.setText(object.getString("health_facility_name"));
                PatientName.setText(object.getString("patient_name"));
                ClaimCode.setText(object.getString("claim_number"));
                MainDg.setText(object.getString("main_dg"));
                SecDg1.setText(object.getString("sec_dg_1"));
                SecDg2.setText(object.getString("sec_dg_2"));
                SecDg3.setText(object.getString("sec_dg_3"));
                SecDg4.setText(object.getString("sec_dg_4"));
                visit_date_from.setText(object.getString("visit_date_from"));
                visit_date_to.setText(object.getString("visit_date_to"));
                dateClaimed.setText(object.getString("date_claimed"));
                date_processed_from.setText(object.getString("visit_date_to"));
                VisitType.setText(object.getString("visit_type"));
                etGuaranteeNo.setText(object.getString("guarantee_number"));
                Claimed.setText(object.getString("claimed"));
                Approved.setText(object.getString("approved"));
                Adjusted.setText(object.getString("adjusted"));
                ClaimStatus.setText(object.getString("claim_status"));
                Explanation.setText(object.getString("explination"));
                Adjustment.setText(object.getString("adjustment"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return v;
    }
}
