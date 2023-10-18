package org.openimis.imisclaims;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.util.TextViewUtils;

import java.io.File;
import java.util.List;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see claimsData and query to check Insuaree numbers
public class ClaimsAdapter extends RecyclerView.Adapter<ClaimsAdapter.Reportmsg> {


    @NonNull
    private final List<Claim> claims;

    String FileName;
    File ClaimFile;

    public int ind = 0;

    private int focusedItem = 0;


    //Constructor
    public ClaimsAdapter(@NonNull List<Claim> claims) {
        this.claims = claims;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener((v, keyCode, event) -> {
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

    @NonNull
    @Override
    public Reportmsg onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim, parent, false);
        return new Reportmsg(row);
    }

    @Override
    public void onBindViewHolder(Reportmsg holder, int position) {
        holder.itemView.setSelected(focusedItem == position);
        holder.bind(claims.get(position));
    }

    @Override
    public int getItemCount() {
        return claims.size();
    }

    public static class Reportmsg extends RecyclerView.ViewHolder {

        private final TextView claimNo;
        private final TextView claimStatus;
        private final TextView healthFacility;
        private final TextView healthFacilityName;
        private final TextView insuranceNo;
        private final TextView dateClaimed;
        private final TextView visitDateFrom;
        private final TextView visitDateTo;

        private Claim claim = null;

        public Reportmsg(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(view -> {
                if (claim != null) {
                    itemView.getContext().startActivity(ClaimReview.newIntent(itemView.getContext(), claim));
                }
            });
            claimNo = itemView.findViewById(R.id.claimNo);
            claimStatus = itemView.findViewById(R.id.claimStatus);
            healthFacility = itemView.findViewById(R.id.healthFacility);
            healthFacilityName = itemView.findViewById(R.id.healthFacilityName);
            insuranceNo = itemView.findViewById(R.id.insuranceNo);
            dateClaimed = itemView.findViewById(R.id.dateClaimed);
            visitDateFrom = itemView.findViewById(R.id.visitDateFrom);
            visitDateTo = itemView.findViewById(R.id.visitDateTo);
        }

        public void bind(Claim claim) {
            this.claim = claim;
            claimNo.setText(claim.getClaimNumber());
            claimStatus.setText(claim.getStatus() != null ? claim.getStatus().name() : null);
            healthFacility.setText(claim.getHealthFacilityCode());
            healthFacilityName.setText(claim.getHealthFacilityName());
            insuranceNo.setText(claim.getInsuranceNumber());
            TextViewUtils.setDate(dateClaimed, claim.getDateClaimed());
            TextViewUtils.setDate(visitDateFrom, claim.getVisitDateFrom());
            TextViewUtils.setDate(visitDateTo, claim.getVisitDateTo());
        }
    }
}
