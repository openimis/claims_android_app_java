package org.openimis.imisclaims;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see claimsData and query to check Insuaree numbers
public class ClaimsAdapter<VH extends TrackSelectionAdapter.ViewHolder> extends RecyclerView.Adapter {

    private JSONArray claims;
    private JSONArray claimsData;

    String claim_no = null;
    String claim_status = null;
    String health_facility = null;
    String insurance_no = null;
    String date_claimed = null;
    String visit_date_from = null;
    String visit_date_to = null;

    private int focusedItem = 0;


    //Constructor
    Context _context;
    public ClaimsAdapter(Context rContext, JSONArray _claims){
        _context = rContext;
        claims = _claims;

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
            health_facility = object.getString("health_facility_name");
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
        public TextView insuranceNo;
        public TextView dateClaimed;
        public TextView visitDateFrom;
        public TextView visitDateTo;


        public Reportmsg(final View itemView) {
            super(itemView);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(_context, ClaimReview.class);
                    _context.startActivity(intent);

/*                    // Redraw the old selection and the new
                    if(overViewclaims.num.size() == 0){
                        overViewclaims.num.add(String.valueOf(getLayoutPosition()));
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
                            overViewclaims.paymentDetails = paymentDetails;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //overViewclaims.PolicyValueToSend += Integer.parseInt(PolicyValue);

                    }*/

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
/*                    Context context = view.getContext();
                    Intent intent = new Intent(context, Viewclaims.class);
                    intent.putExtra("IDENTIFIER", String.valueOf(InternalIdentifier.getText()));
                    context.startActivity(intent);*/
                    return false;
                }
            });


            claimNo = (TextView) itemView.findViewById(R.id.claimNo);
            claimStatus = (TextView) itemView.findViewById(R.id.claimStatus);
            healthFacility = (TextView) itemView.findViewById(R.id.healthFacility);
            insuranceNo = (TextView) itemView.findViewById(R.id.insuranceNo);
            dateClaimed = (TextView) itemView.findViewById(R.id.dateClaimed);
            visitDateFrom = (TextView) itemView.findViewById(R.id.visitDateFrom);
            visitDateTo = (TextView) itemView.findViewById(R.id.visitDateTo);
        }
    }




}
