package org.openimis.imisclaims;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
public class CustomAdapter extends BaseAdapter {
    private Context context;
    public static ArrayList<EditModel> editModelArrayList;
    private static float price;
    public CustomAdapter(Context context, ArrayList<EditModel> editModelArrayList) {
        this.context = context;
        this.editModelArrayList = editModelArrayList;
    }
    @Override
    public int getViewTypeCount() {
        return getCount();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getCount() {
        return editModelArrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return editModelArrayList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.lv_sservice, null, true);
            holder.editCode = (TextView) convertView.findViewById(R.id.tvLvCode);
            holder.editName = (EditText) convertView.findViewById(R.id.tvLvName);
            holder.editQty = (EditText) convertView.findViewById(R.id.tvLvQuantity);
            holder.editPrice = (EditText) convertView.findViewById(R.id.tvLvPrice);
            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }
        holder.editCode.setText(editModelArrayList.get(position).getCode());
        holder.editName.setText(editModelArrayList.get(position).getName());
        holder.editQty.setText(editModelArrayList.get(position).getQty());
        holder.editPrice.setText(editModelArrayList.get(position).getPrice());
        holder.editQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // faire les controle en fonction du package type
                if(!holder.editQty.getText().toString().equals("")){
                    editModelArrayList.get(position).setQty(holder.editQty.getText().toString());
                }else{
                    editModelArrayList.get(position).setQty("0");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                float amount = 0;
                int qtyMax = Integer.valueOf(editModelArrayList.get(position).getQtyMax());
                if(!holder.editQty.getText().toString().equals("")){
                    if (AddServices.packageType.equals("F")){
                        if(Float.valueOf(holder.editQty.getText().toString()) > qtyMax){
                            Toast.makeText(context, context.getResources().getString(R.string.qtyAlert) + " " + qtyMax, Toast.LENGTH_LONG).show();
                            holder.editQty.setText(String.valueOf(qtyMax));
                        }
                    }else if (AddServices.packageType.equals("P")){
                        if(Float.valueOf(holder.editQty.getText().toString()) != qtyMax && Float.valueOf(holder.editQty.getText().toString()) != 0 ){
                            Toast.makeText(context, context.getResources().getString(R.string.qtyAlertAsk) + " " + qtyMax, Toast.LENGTH_LONG).show();
                            holder.editQty.setText(String.valueOf(qtyMax));
                        }
                    }
                }
                for(int i = 0 ; i < editModelArrayList.size(); i++){
                    amount = amount + (Float.valueOf(editModelArrayList.get(i).getQty()) * Float.valueOf(editModelArrayList.get(i).getPrice())) ;
                }
                AddServices.etSAmount.setText(String.valueOf(amount));
            }
        });
        return convertView;
    }
    private class ViewHolder {
        protected EditText editQty;
        protected EditText editName;
        protected EditText editPrice;
        protected TextView editCode;
    }
}