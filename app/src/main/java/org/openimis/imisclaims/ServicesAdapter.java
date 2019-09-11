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
public class ServicesAdapter<VH extends TrackSelectionAdapter.ViewHolder> extends RecyclerView.Adapter {

    private JSONArray Items;
    private JSONArray itemsData;

    String service_code = null;
    String service_name = null;
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
    public ServicesAdapter(Context rContext, JSONArray _Items){
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
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.service,parent,false);

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
            service_code = object.getString("service_code");
            service_name = object.getString("service_name");
            quantity = object.getString("qty");
            price = object.getString("price");
            explanation = object.getString("explanation");
            app_qty = object.getString("adjusted_qty");
            app_price = object.getString("adjusted_price");
            justification = object.getString("justification");
            status = object.getString("claim_status");
            valuated = object.getString("valuated");
            result = object.getString("result");

        } catch (JSONException e) {
            e.printStackTrace();
        }

/*        ((Reportmsg) holder).claimNo.setText(claim_no);
        ((Reportmsg) holder).ItemStatus.setText(claim_status);*/


    }

    @Override
    public int getItemCount() {
        return 10;//Items.length();
    }


    public int getCount(){
        return getItemCount();
    }

    public class Reportmsg extends RecyclerView.ViewHolder{

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


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

/*                    // Redraw the old selection and the new
                    if(overViewItems.num.size() == 0){
                        overViewItems.num.add(String.valueOf(getLayoutPosition()));
                        //itemView.setBackgroundColor(Color.GRAY);
                        checkbox1.setBackgroundResource(R.drawable.checked);

                        try {
                            paymentObject = new JSONObject();
                            //paymentObject.put("Id",String.valueOf(getLayoutPosition()));
                            paymentObject.put("Position",String.valueOf(getLayoutPosition()));
                            paymentObject.put("PolicyId",String.valueOf(PolicyId.getText()));
                            paymentObject.put("internal_identifier",String.valueOf(InternalIdentifier.getText()));
                            paymentObject.put("uploaded_date",String.valueOf(UploadedDate.getText()));
                            paymentDetails.put(paymentObject);
                            overViewItems.paymentDetails = paymentDetails;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //overViewItems.PolicyValueToSend += Integer.parseInt(PolicyValue);

                    }*/

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
/*                    Context context = view.getContext();
                    Intent intent = new Intent(context, ViewItems.class);
                    intent.putExtra("IDENTIFIER", String.valueOf(InternalIdentifier.getText()));
                    context.startActivity(intent);*/
                    return false;
                }
            });


            ServiceCode = (TextView) itemView.findViewById(R.id.ServiceCode);
            ServiceName = (TextView) itemView.findViewById(R.id.ServiceName);
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
