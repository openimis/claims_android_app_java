package org.openimis.imisclaims;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import static org.openimis.imisclaims.ClaimActivity.Path;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see claimsData and query to check Insuaree numbers
public class ClaimsAdapter<VH extends TrackSelectionAdapter.ViewHolder> extends RecyclerView.Adapter {

    Global global;

    private JSONArray claims;
    private JSONArray claimsData;

    String claim_no = null;
    String claim_status = null;
    String health_facility = null;
    String health_facility_name = null;
    String insurance_no = null;
    String date_claimed = null;
    String visit_date_from = null;
    String visit_date_to = null;

    JSONObject obj = null;

    String FileName;
    File ClaimFile;

    public int ind = 0;

    private int focusedItem = 0;


    //Constructor
    Context _context;
    public ClaimsAdapter(Context rContext, JSONArray _claims){
        _context = rContext;
        claims = _claims;
        global = (Global)rContext.getApplicationContext();

        try {
            for (int i = 0; i < _claims.length(); i++) {
                JSONObject claim = claims.getJSONObject(i);
                claim.put("date_claimed", parseDate(claims.getJSONObject(i).getString("date_claimed")));
                claim.put("visit_date_from", parseDate(claims.getJSONObject(i).getString("visit_date_from")));
                claim.put("visit_date_to", parseDate(claims.getJSONObject(i).getString("visit_date_to")));
            }
        } catch ( JSONException e )
        {
            e.printStackTrace();
        }
    }



    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }

                return false;
            }
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(focusedItem);
            focusedItem = tryFocusItem;
            notifyItemChanged(focusedItem);
            lm.scrollToPosition(focusedItem);
            return true;
        }

        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim,parent,false);

        Reportmsg view = new Reportmsg(row);
        return view;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();

                if (query.isEmpty()) {
                    claimsData = claims;
                } else {
                    for(int i=0; i<=claims.length();i++){
                        try {
                            if (claims.getString(i).toLowerCase().contains(query.toLowerCase())) {
                                claimsData.put(claims.getString(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.count = claimsData.length();
                results.values = claimsData;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                //claims = results.values;
                claimsData = (JSONArray) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setSelected(focusedItem == position);
        claimsData = claims;

        try {
            JSONObject object = claimsData.getJSONObject(position);
            claim_no = object.getString("claim_number");
            claim_status = object.getString("claim_status");
            health_facility = object.getString("health_facility_code");
            health_facility_name = object.getString("health_facility_name");
            insurance_no = object.getString("insurance_number");
            date_claimed = object.getString("date_claimed");
            visit_date_from = object.getString("visit_date_from");
            visit_date_to = object.getString("visit_date_to");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((Reportmsg) holder).claimNo.setText(claim_no);
        ((Reportmsg) holder).claimStatus.setText(claim_status);
        ((Reportmsg) holder).healthFacility.setText(health_facility);
        ((Reportmsg) holder).healthFacilityName.setText(health_facility_name);
        ((Reportmsg) holder).insuranceNo.setText(insurance_no);
        ((Reportmsg) holder).dateClaimed.setText(date_claimed);
        ((Reportmsg) holder).visitDateFrom.setText(visit_date_from);
        ((Reportmsg) holder).visitDateTo.setText(visit_date_to);


    }

    @Override
    public int getItemCount() {
        return claims.length();
    }


    public int getCount(){
        return getItemCount();
    }

    public class Reportmsg extends RecyclerView.ViewHolder{

        public TextView claimNo;
        public TextView claimStatus;
        public TextView healthFacility;
        public TextView healthFacilityName;
        public TextView insuranceNo;
        public TextView dateClaimed;
        public TextView visitDateFrom;
        public TextView visitDateTo;


        public Reportmsg(final View itemView) {
            super(itemView);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                       obj = claims.getJSONObject(getAdapterPosition());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(_context, ClaimReview.class);
                    intent.putExtra("claims", String.valueOf(obj));
                    _context.startActivity(intent);

                }
            });

            claimNo = (TextView) itemView.findViewById(R.id.claimNo);
            claimStatus = (TextView) itemView.findViewById(R.id.claimStatus);
            healthFacility = (TextView) itemView.findViewById(R.id.healthFacility);
            healthFacilityName = (TextView) itemView.findViewById(R.id.healthFacilityName);
            insuranceNo = (TextView) itemView.findViewById(R.id.insuranceNo);
            dateClaimed = (TextView) itemView.findViewById(R.id.dateClaimed);
            visitDateFrom = (TextView) itemView.findViewById(R.id.visitDateFrom);
            visitDateTo = (TextView) itemView.findViewById(R.id.visitDateTo);
        }
    }

    protected AlertDialog ShowDialog(String msg){
        return new AlertDialog.Builder(_context)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(_context.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //et.requestFocus();
                        return;
                    }
                }).show();
    }

    private String parseDate(String date)
    {
        SimpleDateFormat fromApiFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat databaseFormat = new SimpleDateFormat("dd/MM/yyyy");
        String parsedDate = null;

        try {
            parsedDate = databaseFormat.format(fromApiFormat.parse(date));
        } catch ( ParseException e ) {
            e.printStackTrace();
            parsedDate = date;
        }
        return parsedDate;
    }
}
