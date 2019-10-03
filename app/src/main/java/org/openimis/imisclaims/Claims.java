package org.openimis.imisclaims;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Claims extends AppCompatActivity {

    ClaimsAdapter claimsAdapter;
    RecyclerView listOfClaims;
    JSONArray claimJson;


    String claims = "";

    JSONObject object = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claims);
        Intent intent = getIntent();
        claims = intent.getStringExtra("claims");

        try {
            object = new JSONObject(claims);
            claimJson = new JSONArray(object.getString("claims"));


        } catch (JSONException e) {
            e.printStackTrace();
        }
        fillClaims();

    }

    public void fillClaims() {
        //claimJson = new JSONArray(claims);//clientAndroidInterface.getRecordedPolicies(InsuranceNumber,OtherNames,LastName,InsuranceProduct,UploadedFrom,UploadedTo,RadioRenewal,RequestedFrom,RequestedTo, PaymentType, RadioSms);//OrderArray;
        LayoutInflater li = LayoutInflater.from(Claims.this);
        View promptsView = li.inflate(R.layout.activity_search_claims, null);
        listOfClaims = (RecyclerView) findViewById(R.id.listOfClaims);
        claimsAdapter = new ClaimsAdapter(this,claimJson);
        listOfClaims.setLayoutManager(new LinearLayoutManager(this));
        //PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        listOfClaims.setAdapter(claimsAdapter);
    }


}
