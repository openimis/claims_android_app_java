package org.openimis.imisclaims;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hiren on 06/09/2019.
 */

public class ItemsFragment extends Fragment {

    ItemsAdapter claimsAdapter;
    RecyclerView listOfClaims;
    JSONArray claimJson;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_items, container, false);

        String c = ((ClaimReview) getContext()).claimText;
        try {
            JSONObject object = new JSONObject(c);
            claimJson = new JSONArray(object.getString("items"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (claimJson.length() != 0) {
            fillClaims(v);
        }


        return v;
    }

    public void fillClaims(View v) {
        //claimJson = new JSONArray();//clientAndroidInterface.getRecordedPolicies(InsuranceNumber,OtherNames,LastName,InsuranceProduct,UploadedFrom,UploadedTo,RadioRenewal,RequestedFrom,RequestedTo, PaymentType, RadioSms);//OrderArray;
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.activity_search_claims, null);
        listOfClaims = (RecyclerView) v.findViewById(R.id.listOfItems);
        claimsAdapter = new ItemsAdapter(getContext(), claimJson);
        listOfClaims.setLayoutManager(new LinearLayoutManager(getContext()));
        //PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        listOfClaims.setAdapter(claimsAdapter);
    }
}
