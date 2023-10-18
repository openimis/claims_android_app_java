package org.openimis.imisclaims;



import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import org.openimis.imisclaims.domain.entity.Claim;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see itemsData and query to check Insuaree numbers
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.Reportmsg> {

    private int focusedItem = 0;
    private final Claim claim;

    //Constructor
    public ItemsAdapter(Claim claim) {
        this.claim = claim;
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
    public Reportmsg onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new Reportmsg(row);
    }

    @Override
    public void onBindViewHolder(Reportmsg holder, int position) {
        holder.itemView.setSelected(focusedItem == position);
        Claim.Medication medication = claim.getMedications().get(position);

        holder.ItemCode.setText(medication.getCode());
        holder.ItemName.setText(medication.getName());
        holder.Quantity.setText(medication.getQuantity());
        holder.Price.setText(String.valueOf(medication.getPrice()));
        holder.Explanation.setText(medication.getExplanation());
        holder.AppQty.setText(medication.getQuantityApproved());
        holder.AppPrice.setText(medication.getPriceAdjusted());
        holder.Justification.setText(medication.getJustification());
        holder.Status.setText(""); // previous code took the status from the claim then got commented out
        holder.Valuated.setText(medication.getPriceValuated());
        holder.Result.setText(null); //item_result
    }

    @Override
    public int getItemCount() {
        return claim.getMedications().size();
    }

    public static class Reportmsg extends RecyclerView.ViewHolder {

        public TextView ItemCode;
        public TextView ItemName;
        public TextView Quantity;
        public TextView Price;
        public TextView Explanation;
        public TextView AppQty;
        public TextView AppPrice;
        public TextView Justification;
        public TextView Status;
        public TextView Valuated;
        public TextView Result;

        public Reportmsg(final View itemView) {
            super(itemView);

            ItemCode = itemView.findViewById(R.id.ItemCode);
            ItemName = itemView.findViewById(R.id.ItemName);
            Quantity = itemView.findViewById(R.id.Qty);
            Price = itemView.findViewById(R.id.Price);
            Explanation = itemView.findViewById(R.id.Explanation);
            AppQty = itemView.findViewById(R.id.AppQty);
            AppPrice = itemView.findViewById(R.id.AppPrice);
            Justification = itemView.findViewById(R.id.Justification);
            Status = itemView.findViewById(R.id.Status);
            Valuated = itemView.findViewById(R.id.valuated);
            Result = itemView.findViewById(R.id.Result);
        }
    }
}
