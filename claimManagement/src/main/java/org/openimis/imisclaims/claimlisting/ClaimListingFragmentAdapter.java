package org.openimis.imisclaims.claimlisting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.ClaimActivity;
import org.openimis.imisclaims.R;
import org.openimis.imisclaims.tools.Log;

/**
 * Adapter controlling a single claim listing page
 */
public class ClaimListingFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CLAIMLISTINGFRAGMENTADAPTER";
    private Context context;
    private ClaimListingPage page;
    public JSONArray data;

    public ClaimListingFragmentAdapter(Context context, ClaimListingPage page, JSONArray data) {
        this.context = context;
        this.page = page;
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
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

            claimListingViewHolder.itemView.setOnClickListener((view) -> {
                try {
                    context.startActivity(
                            ClaimActivity.newIntent(
                                    context,
                                    /* claimUUID = */ row.getString("ClaimUUID"),
                                    /* readOnly = */ page != ClaimListingPage.ENTERED_PAGE
                            )
                    );
                } catch (JSONException | IndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "Error while handling item click", e);
                }
            });

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
        public View itemView;
        public TextView claimCode;
        public TextView claimDate;
        public TextView insureeNumber;
        public TextView totalClaimed;

        public ClaimListingViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            claimCode = itemView.findViewById(R.id.claimCode);
            claimDate = itemView.findViewById(R.id.claimDate);
            insureeNumber = itemView.findViewById(R.id.insureeNumber);
            totalClaimed = itemView.findViewById(R.id.totalClaimed);
        }
    }
}
