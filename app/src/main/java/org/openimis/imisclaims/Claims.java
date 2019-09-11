package org.openimis.imisclaims;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONArray;

public class Claims extends AppCompatActivity {

    ClaimsAdapter claimsAdapter;
    RecyclerView listOfClaims;
    JSONArray claimJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claims);

        fillClaims();

    }

    public void fillClaims(){
        claimJson = new JSONArray();//clientAndroidInterface.getRecordedPolicies(InsuranceNumber,OtherNames,LastName,InsuranceProduct,UploadedFrom,UploadedTo,RadioRenewal,RequestedFrom,RequestedTo, PaymentType, RadioSms);//OrderArray;
        LayoutInflater li = LayoutInflater.from(Claims.this);
        View promptsView = li.inflate(R.layout.activity_search_claims, null);
        listOfClaims = (RecyclerView) findViewById(R.id.listOfClaims);
        claimsAdapter = new ClaimsAdapter(this,claimJson);
        listOfClaims.setLayoutManager(new LinearLayoutManager(this));
        //PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        listOfClaims.setAdapter(claimsAdapter);
    }
}
