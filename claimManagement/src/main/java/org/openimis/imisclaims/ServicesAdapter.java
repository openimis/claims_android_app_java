package org.openimis.imisclaims;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openimis.imisclaims.domain.entity.Claim;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see serviceData and query to check Insuaree numbers
public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.Reportmsg> {

    private int focusedItem = 0;

    private final Claim claim;

    //Constructor

    public ServicesAdapter(Claim claim){
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
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.service,parent,false);
        return new Reportmsg(row);
    }

    @Override
    public void onBindViewHolder(Reportmsg holder, int position) {
        holder.itemView.setSelected(focusedItem == position);
        Claim.Service service = claim.getServices().get(position);

        holder.ServiceCode.setText(service.getCode());
        holder.ServiceName.setText(service.getName());
        holder.Quantity.setText(service.getQuantity());
        holder.Price.setText(String.valueOf(service.getPrice()));
        holder.Explanation.setText(service.getExplanation());
        holder.AppQty.setText(service.getQuantityApproved());
        holder.AppPrice.setText(service.getPriceAdjusted());
        holder.Justification.setText(service.getJustification());
        holder.Status.setText(""); // previous code took the status from the claim then got commented out
        holder.Valuated.setText(service.getPriceValuated());
        holder.Result.setText(null); // TODO service_result
    }

    @Override
    public int getItemCount() {
        return claim.getServices().size();
    }


    public static class Reportmsg extends RecyclerView.ViewHolder{

        public TextView ServiceCode;
        public TextView ServiceName;
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

            ServiceCode = itemView.findViewById(R.id.ServiceCode);
            ServiceName = itemView.findViewById(R.id.ServiceName);
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
