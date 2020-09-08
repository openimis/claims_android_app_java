package org.openimis.imisclaims;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see itemsData and query to check Insuaree numbers
public class ItemsAdapter<VH extends TrackSelectionAdapter.ViewHolder> extends RecyclerView.Adapter {

    private JSONArray Items;
    private JSONArray itemsData;

    String item_code = null;
    String item_name = null;
    String quantity = null;
    String price = null;
    String explanation = null;
    String app_qty = null;
    String app_price = null;
    String justification = null;
    String status = null;
    String valuated = null;
    String result = null;

    private int focusedItem = 0;


    //Constructor
    Context _context;
    public ItemsAdapter(Context rContext, JSONArray _Items){
        _context = rContext;
        Items = _Items;

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
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);

        Reportmsg view = new Reportmsg(row);
        return view;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();

                if (query.isEmpty()) {
                    itemsData = Items;
                } else {
                    for(int i=0; i<=Items.length();i++){
                        try {
                            if (Items.getString(i).toLowerCase().contains(query.toLowerCase())) {
                                itemsData.put(Items.getString(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.count = itemsData.length();
                results.values = itemsData;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                //Items = results.values;
                itemsData = (JSONArray) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setSelected(focusedItem == position);
        itemsData = Items;

        try {
            JSONObject object = itemsData.getJSONObject(position);
            item_code = (object.getString("item_code").equals("null")?"":object.getString("item_code"));
            item_name = (object.getString("item").equals("null")?"":object.getString("item"));
            quantity = (object.getString("item_qty").equals("null")?"":object.getString("item_qty"));
            price = (object.getString("item_price").equals("null")?"":object.getString("item_price"));
            explanation = (object.getString("item_explination").equals("null")?"":object.getString("item_explination"));
            app_qty = (object.getString("item_adjusted_qty").equals("null")?"":object.getString("item_adjusted_qty"));
            app_price = (object.getString("item_adjusted_price").equals("null")?"":object.getString("item_adjusted_price"));
            justification = (object.getString("item_justificaion").equals("null")?"":object.getString("item_justificaion"));
            status = "";//object.getString("claim_status");
            valuated = (object.getString("item_valuated").equals("null")?"":object.getString("item_valuated"));
            result = (object.getString("item_result").equals("null")?"":object.getString("item_result"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((Reportmsg) holder).ItemCode.setText(item_code);
        ((Reportmsg) holder).ItemName.setText(item_name);
        ((Reportmsg) holder).Quantity.setText(quantity);
        ((Reportmsg) holder).Price.setText(price);
        ((Reportmsg) holder).Explanation.setText(explanation);
        ((Reportmsg) holder).AppQty.setText(app_qty);
        ((Reportmsg) holder).AppPrice.setText(app_price);
        ((Reportmsg) holder).Justification.setText(justification);
        ((Reportmsg) holder).Status.setText(status);
        ((Reportmsg) holder).Valuated.setText(valuated);
        ((Reportmsg) holder).Result.setText(result);


    }

    @Override
    public int getItemCount() {
        return Items.length();
    }


    public int getCount(){
        return getItemCount();
    }

    public class Reportmsg extends RecyclerView.ViewHolder{

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

            ItemCode = (TextView) itemView.findViewById(R.id.ItemCode);
            ItemName = (TextView) itemView.findViewById(R.id.ItemName);
            Quantity = (TextView) itemView.findViewById(R.id.Qty);
            Price = (TextView) itemView.findViewById(R.id.Price);
            Explanation = (TextView) itemView.findViewById(R.id.Explanation);
            AppQty = (TextView) itemView.findViewById(R.id.AppQty);
            AppPrice = (TextView) itemView.findViewById(R.id.AppPrice);
            Justification = (TextView) itemView.findViewById(R.id.Justification);
            Status = (TextView) itemView.findViewById(R.id.Status);
            Valuated = (TextView) itemView.findViewById(R.id.valuated);
            Result = (TextView) itemView.findViewById(R.id.Result);
        }
    }
}
