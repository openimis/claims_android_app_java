package org.openimis.imisclaims;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.openimis.imisclaims.ClaimActivity.Path;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see claimsData and query to check Insuaree numbers
public class ClaimsAdapter<VH extends TrackSelectionAdapter.ViewHolder> extends RecyclerView.Adapter {

    Global global = new Global();

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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    try {
                        obj = claims.getJSONObject(getAdapterPosition());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RestoreDialogBox(claimNo.getText().toString(),claimStatus.getText().toString(),healthFacility.getText().toString(),healthFacilityName.getText().toString(),insuranceNo.getText().toString(),dateClaimed.getText().toString(),visitDateFrom.getText().toString(),visitDateTo.getText().toString());
                    return false;
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

    public void RestoreDialogBox(String _claimNo,String _claimStatus,String _healthFacility,String _healthFacilityName,String _insuranceNo,String _dateClaimed,String _visitDateFrom,String _visitDateTo) {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(_context);
        View promptsView = li.inflate(R.layout.restore_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                _context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        //final TextView username = (TextView) promptsView.findViewById(R.id.UserName);
        //final TextView password = (TextView) promptsView.findViewById(R.id.Password);


        TextView claimNo = (TextView) promptsView.findViewById(R.id.claimNo);
        TextView claimStatus = (TextView) promptsView.findViewById(R.id.claimStatus);
        TextView healthFacility = (TextView) promptsView.findViewById(R.id.healthFacility);
        TextView healthFacilityName = (TextView) promptsView.findViewById(R.id.healthFacilityName);
        TextView insuranceNo = (TextView) promptsView.findViewById(R.id.insuranceNo);
        TextView dateClaimed = (TextView) promptsView.findViewById(R.id.dateClaimed);
        TextView visitDateFrom = (TextView) promptsView.findViewById(R.id.visitDateFrom);
        TextView visitDateTo = (TextView) promptsView.findViewById(R.id.visitDateTo);


        claimNo.setText(_claimNo);
        claimStatus.setText(_claimStatus);
        healthFacility.setText(_healthFacility);
        healthFacilityName.setText(_healthFacilityName);
        if(_claimStatus.equals("Rejected")){
            insuranceNo.setText(_insuranceNo);
        }else {
            insuranceNo.setText("");
        }
        dateClaimed.setText(_dateClaimed);
        visitDateFrom.setText(_visitDateFrom);
        visitDateTo.setText(_visitDateTo);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Restore,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                WriteXML();
                                WriteJSON();

                                ShowDialog(_context.getResources().getString(R.string.ClaimRestored));

                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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

    private void WriteXML(){

        //Create all the directories required
        File MyDir = new File(Path);
        MyDir.mkdir();

        File DirRejected = new File(Path + "RejectedClaims");
        DirRejected.mkdir();

        File DirAccepted = new File(Path + "AcceptedClaims");
        DirAccepted.mkdir();

        //Create a file name
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        try {

            FileName = "Claim_" + obj.getString("health_facility_code") + "_" + obj.getString("claim_number") + "_" + d + ".xml";
            ClaimFile = new File(MyDir,FileName);

            FileOutputStream fos = new FileOutputStream(ClaimFile);

            XmlSerializer serializer = Xml.newSerializer();

            serializer.setOutput(fos, "UTF-8");

            serializer.startDocument(null, Boolean.valueOf(true));

            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            //<Claim>
            serializer.startTag(null, "Claim");

            //<Details>
            serializer.startTag(null, "Details");

            //ClaimDate
            serializer.startTag(null, "ClaimDate");
            format = new SimpleDateFormat("dd/MM/yyyy");
            d = format.format(cal.getTime());
            serializer.text(d);
            serializer.endTag(null, "ClaimDate");

            //HFCOde
            serializer.startTag(null, "HFCode");
            serializer.text(obj.getString("health_facility_code"));
            serializer.endTag(null, "HFCode");

            //Claim Admin
            serializer.startTag(null, "ClaimAdmin");
            serializer.text(global.getOfficerCode().toString());
            serializer.endTag(null, "ClaimAdmin");

            //ClaimCode
            serializer.startTag(null, "ClaimCode");
            serializer.text("@"+obj.getString("claim_number"));
            serializer.endTag(null, "ClaimCode");

            //GuaranteeNo
            serializer.startTag(null, "GuaranteeNo");
            serializer.text(obj.getString("guarantee_number"));
            serializer.endTag(null, "GuaranteeNo");

            //CHFID
            serializer.startTag(null, "CHFID");
            serializer.text(obj.getString("insurance_number"));
            serializer.endTag(null, "CHFID");

            //StartDate
            serializer.startTag(null, "StartDate");
            serializer.text(obj.getString("visit_date_from"));
            serializer.endTag(null, "StartDate");

            //EndDate
            serializer.startTag(null, "EndDate");
            serializer.text(obj.getString("visit_date_to"));
            serializer.endTag(null, "EndDate");

            //ICDCode
            serializer.startTag(null, "ICDCode");
            serializer.text(obj.getString("main_dg"));
            serializer.endTag(null, "ICDCode");

            //Comment
            serializer.startTag(null, "Comment");
            serializer.text(obj.getString("explination"));
            serializer.endTag(null, "Comment");

            //Total
            serializer.startTag(null,"Total");
            serializer.text("");
            serializer.endTag(null,"Total");

            //Diagnosis1
            serializer.startTag(null, "ICDCode1");
            serializer.text(obj.getString("sec_dg_1"));
            serializer.endTag(null, "ICDCode1");

            //Diagnosis2
            serializer.startTag(null, "ICDCode2");
            serializer.text(obj.getString("sec_dg_2"));
            serializer.endTag(null, "ICDCode2");

            //Diagnosis3
            serializer.startTag(null, "ICDCode3");
            serializer.text(obj.getString("sec_dg_3"));
            serializer.endTag(null, "ICDCode3");

            //Diagnosis4
            serializer.startTag(null, "ICDCode4");
            serializer.text(obj.getString("sec_dg_4"));
            serializer.endTag(null, "ICDCode4");

            //VisitType
            serializer.startTag(null, "VisitType");
            serializer.text(obj.getString("visit_type"));
            serializer.endTag(null, "VisitType");


            serializer.endTag(null, "Details");
            //</Details>

            //<Items>
            serializer.startTag(null, "Items");

            JSONArray lvItemList = new JSONArray(obj.getString("items"));

            for(int i=0;i<lvItemList.length();i++){
                JSONObject object = lvItemList.getJSONObject(i);
                //<Item>
                serializer.startTag(null,"Item");

                //Code
                serializer.startTag(null, "ItemCode");
                serializer.text(object.getString("item_code"));
                serializer.endTag(null, "ItemCode");

                //Price
                serializer.startTag(null, "ItemPrice");
                serializer.text(object.getString("item_price"));
                serializer.endTag(null, "ItemPrice");

                //Quantity
                serializer.startTag(null, "ItemQuantity");
                serializer.text(object.getString("item_qty"));
                serializer.endTag(null, "ItemQuantity");

                serializer.endTag(null,"Item");
                //</Item>
            }

            serializer.endTag(null, "Items");
            //</Items>


            //<Services>
            serializer.startTag(null, "Services");
            JSONArray lvServiceList = new JSONArray(obj.getString("services"));
            for(int i=0;i<lvServiceList.length();i++){
                JSONObject object = lvServiceList.getJSONObject(i);
                //<Service>
                serializer.startTag(null, "Service");

                //Code
                serializer.startTag(null, "ServiceCode");
                serializer.text(object.getString("service_code"));
                serializer.endTag(null, "ServiceCode");

                //Price
                serializer.startTag(null, "ServicePrice");
                serializer.text(object.getString("service_price"));
                serializer.endTag(null, "ServicePrice");

                //Quantity
                serializer.startTag(null, "ServiceQuantity");
                serializer.text(object.getString("service_qty"));
                serializer.endTag(null, "ServiceQuantity");

                //<Service>
                serializer.endTag(null, "Service");
            }

            serializer.endTag(null, "Services");
            //</Services>

            serializer.endTag(null, "Claim");
            //</Claim>

            serializer.endDocument();
            serializer.flush();
            fos.flush();
            fos.close();


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void WriteJSON(){


        //Create all the directories required
        File MyDir = new File(Path);
        MyDir.mkdir();

        //sql = new SQLHandler(this);
        //sql.onOpen(db);

        //Create a file name
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        try {

            FileName = "ClaimJSON_" + obj.getString("health_facility_code") + "_" + obj.getString("claim_number") + "_" + d + ".txt";
            ClaimFile = new File(MyDir,FileName);

            JSONObject jsonObject = new JSONObject();
            JSONObject FullObject = new JSONObject();
            JSONObject ClaimObject = new JSONObject();


            format = new SimpleDateFormat("dd/MM/yyyy");
            d = format.format(cal.getTime());
            //ClaimDate
            ClaimObject.put("ClaimDate", d);
            //HFCOde
            ClaimObject.put("HFCode", obj.getString("health_facility_code"));
            //Claim Admin
            ClaimObject.put("ClaimAdmin", global.getOfficerCode().toString());
            //ClaimCode
            ClaimObject.put("ClaimCode", "@"+obj.getString("claim_number"));
            //GuaranteeNo
            ClaimObject.put("GuaranteeNo", obj.getString("guarantee_number"));
            //CHFID
            ClaimObject.put("CHFID", obj.getString("insurance_number"));
            //StartDate
            ClaimObject.put("StartDate", obj.getString("visit_date_from"));
            //EndDate
            ClaimObject.put("EndDate", obj.getString("visit_date_to"));
            //ICDCode
            ClaimObject.put("ICDCode", obj.getString("main_dg"));
            //Comment
            ClaimObject.put("Comment", obj.getString("explination"));
            //Total
            ClaimObject.put("Total", "");
            //Diagnosis1
            ClaimObject.put("ICDCode1", obj.getString("sec_dg_1"));
            //Diagnosis2
            ClaimObject.put("ICDCode2", obj.getString("sec_dg_2"));
            //Diagnosis3
            ClaimObject.put("ICDCode3", obj.getString("sec_dg_3"));
            //Diagnosis4
            ClaimObject.put("ICDCode4", obj.getString("sec_dg_4"));
            //VisitType
            ClaimObject.put("VisitType", obj.getString("visit_type"));


            FullObject.put("Details",ClaimObject);
            //</Details>

            //Items
            ClaimObject = new JSONObject();


            JSONArray lvItemList = new JSONArray(obj.getString("items"));
            JSONArray ItemsArray = new JSONArray();

            for(int i=0;i<lvItemList.length();i++){
                JSONObject object = lvItemList.getJSONObject(i);
                JSONObject SubObjectItems = new JSONObject();
                JSONObject ItemObject = new JSONObject();
                //Code
                ItemObject.put("ItemCode",object.getString("item"));
                //Price
                ItemObject.put("ItemPrice",object.getString("item_price"));
                //Quantity
                ItemObject.put("ItemQuantity",object.getString("item_qty"));
                SubObjectItems.put("Item",ItemObject);

                ItemsArray.put(SubObjectItems);

            }
            //</Items>
            FullObject.put("Items",ItemsArray);


            //<Services>
            ClaimObject = new JSONObject();

            JSONArray lvServiceList = new JSONArray(obj.getString("services"));

            JSONArray ServicesArray = new JSONArray();

            for(int i=0;i<lvServiceList.length();i++){
                JSONObject object = lvServiceList.getJSONObject(i);
                JSONObject SubObjectServices = new JSONObject();
                JSONObject ServiceObject = new JSONObject();
                //Code
                ServiceObject.put("ServiceCode",object.getString("service"));
                //Price
                ServiceObject.put("ServicePrice",object.getString("service_price"));
                //Quantity
                ServiceObject.put("ServiceQuantity",object.getString("service_qty"));
                //</Service>
                SubObjectServices.put("Service",ServiceObject);

                ServicesArray.put(SubObjectServices);
            }
            //</Services>
            FullObject.put("Services",ServicesArray);

            //</Claim>
            jsonObject.put("Claim",FullObject);

            try {
                String dir = Environment.getExternalStorageDirectory() + File.separator + "IMIS/";
                FileOutputStream fOut = new FileOutputStream(dir+FileName);
                OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
                myOutWriter.append(jsonObject.toString());
                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
