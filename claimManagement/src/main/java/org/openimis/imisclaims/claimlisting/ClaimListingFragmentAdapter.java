package org.openimis.imisclaims.claimlisting;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.R;
import org.openimis.imisclaims.tools.Log;

/**
 * Adapter controlling a single claim listing page
 */
public class ClaimListingFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CLAIMLISTINGFRAGMENTADAPTER";

    public JSONArray data;

    public ClaimListingFragmentAdapter(JSONArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_claim_listing_recycler_item, viewGroup, false);

        return new ClaimListingViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ClaimListingViewHolder claimListingViewHolder = (ClaimListingViewHolder) viewHolder;

        try {
            JSONObject row = data.getJSONObject(i);

            claimListingViewHolder.claimCode.setText(row.getString("ClaimCode"));
            claimListingViewHolder.claimDate.setText(row.getString("ClaimDate"));
            claimListingViewHolder.insureeNumber.setText(row.getString("InsureeNumber"));
            claimListingViewHolder.totalClaimed.setText(row.getString("TotalClaimed"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while reading data", e);
        }
    }

    @Override
    public int getItemCount() {
        return data.length();
    }


    /**
     * View holder containing references and events to specific fields of a claim list recycler view
     */
    private static class ClaimListingViewHolder extends RecyclerView.ViewHolder {
        TextView claimCode;
        TextView claimDate;
        TextView insureeNumber;
        TextView totalClaimed;

        public ClaimListingViewHolder(@NonNull View itemView) {
            super(itemView);
            claimCode = itemView.findViewById(R.id.claimCode);
            claimDate = itemView.findViewById(R.id.claimDate);
            insureeNumber = itemView.findViewById(R.id.insureeNumber);
            totalClaimed = itemView.findViewById(R.id.totalClaimed);
        }
    }
}
