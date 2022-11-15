package org.openimis.imisclaims;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.openimis.imisclaims.tools.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapServices extends ImisActivity {
    ListView lvMapServices;
    CheckBox chkAll, chk;
    EditText etSearchServices;

    ArrayList<HashMap<String, Object>> ServiceList = new ArrayList<>();

    HashMap<String, Object> oService;
    ServiceAdapter alAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapservices);

        actionBar.setTitle(getResources().getString(R.string.app_name_claim));

        lvMapServices = (ListView) findViewById(R.id.lvMapServices);
        chkAll = (CheckBox) findViewById(R.id.chkAllServices);
        chk = (CheckBox) findViewById(R.id.chkMap);
        etSearchServices = (EditText) findViewById(R.id.etSearchServices);

        etSearchServices.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (alAdapter != null) {
                    alAdapter.getFilter().filter(charSequence.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        lvMapServices.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                oService = (HashMap<String, Object>) parent.getItemAtPosition(position);
                boolean checked = (Boolean) oService.get("isMapped");
                oService.put("isMapped", !checked);
                ServiceList.set(position, oService);
                alAdapter.notifyDataSetChanged();

                if (checked) {
                    chkAll.setChecked(false);
                }

            }
        });

        chkAll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                CheckUncheckAll(chkAll.isChecked());

            }
        });

        try {
            BindItemList();
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.NoStatFound), Toast.LENGTH_LONG).show();
        }


    }

    public void BindItemList() {
        //String[] Items = {"Item1","Item2","Item3","Item4","Item5"};

        //SQLHandler sql = new SQLHandler(null, null, null, 3);
        SQLHandler sql = new SQLHandler(this);

        Cursor c = sql.getMapping("S");
        boolean isMapped = false;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("Code", c.getString(0));
            item.put("Name", c.getString(1));
            if ((c.getString(2)) == null) isMapped = false;
            else isMapped = true;
            item.put("isMapped", isMapped);
            ServiceList.add(item);
        }
        c.close();

        alAdapter = new ServiceAdapter(MapServices.this, ServiceList, R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode, R.id.tvMapName, R.id.chkMap});


        //lvMapServices.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,Items));


        try {
            lvMapServices.setAdapter(alAdapter);
            lvMapServices.setItemsCanFocus(false);
            lvMapServices.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void CheckUncheckAll(boolean isChecked) {
        for (int i = 0; i < ServiceList.size(); i++) {
            oService = (HashMap<String, Object>) ServiceList.get(i);
            oService.put("isMapped", isChecked);
            ServiceList.set(i, oService);
            alAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mif = getMenuInflater();
        mif.inflate(R.menu.mapping, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuSave:
                progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);
                progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.Saving));
                new Thread() {
                    public void run() {
                        int save = Save();
                        if (save == 2) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShowDialog(getResources().getString(R.string.MemoryFull));
                                }
                            });

                        } else if (save == 1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShowDialog(getResources().getString(R.string.SelectItems));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShowDialog(getResources().getString(R.string.saved_successfully));
                                }
                            });
                        }
                        progressDialog.dismiss();
                    }
                }.start();

                return true;
            case R.id.mnuCancel:
                finish();
                return true;
            default:
                onBackPressed();
                return true;
        }
    }

    private int Save() {
        int count = 0;
        sqlHandler.ClearMapping("S");
        for (int i = 0; i < ServiceList.size(); i++) {
            oService = (HashMap<String, Object>) ServiceList.get(i);
            boolean checked = (Boolean) oService.get("isMapped");
            if (checked) {
                count++;
                if (!sqlHandler.InsertMapping(oService.get("Code").toString(), oService.get("Name").toString(), "S")) {
                    return 2;
                }

            }
        }
        if (count == 0) {
            return 1;
        }
        return 0;
    }

    protected AlertDialog ShowDialog(String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //et.requestFocus();
                        return;
                    }
                }).show();
    }

    private class ServiceAdapter extends SimpleAdapter {

        private ArrayList<HashMap<String, Object>> OriginalList, FilteredList;
        private ArrayList<HashMap<String, Object>> ItemList;
        private HashMap<String, Object> TempData;
        private ItemFilter filter;
        //private Context cntx;


        public ServiceAdapter(Context context, List<? extends Map<String, ?>> ItemList, int resource, String[] from, int[] to) {
            super(context, ItemList, resource, from, to);
            //this.cntx = context;
            this.ItemList = new ArrayList<HashMap<String, Object>>();
            this.ItemList.addAll((Collection<? extends HashMap<String, Object>>) ItemList);
            this.OriginalList = new ArrayList<HashMap<String, Object>>();
            this.OriginalList.addAll((Collection<? extends HashMap<String, Object>>) ItemList);

        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new ItemFilter();
            }
            return filter;
        }

        private class ViewHolder {
            TextView Code;
            TextView Name;
            CheckBox isMapped;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            Log.v("ConvertView", String.valueOf(position));
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.mappinglist, null);

                holder = new ViewHolder();
                holder.Code = convertView.findViewById(R.id.tvMapCode);
                holder.Name = convertView.findViewById(R.id.tvMapName);
                holder.isMapped = convertView.findViewById(R.id.chkMap);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position > ItemList.size())
                return convertView;

            TempData = ItemList.get(position);
            holder.Code.setText((String) TempData.get("Code"));
            holder.Name.setText((String) TempData.get("Name"));
            holder.isMapped.setChecked((Boolean) TempData.get("isMapped"));

            return convertView;
        }

        private class ItemFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                ArrayList<HashMap<String, Object>> FilteredItems = new ArrayList<HashMap<String, Object>>();
                ;

                if (constraint != null && constraint.toString().length() > 0) {

                    for (int i = 0; i < OriginalList.size(); i++) {
                        HashMap<String, Object> oItem = OriginalList.get(i);
                        if (oItem.get("Code").toString().toLowerCase().contains(constraint) || oItem.get("Name").toString().toLowerCase().contains(constraint)) {
                            FilteredItems.add(oItem);
                        }
                        results.count = FilteredItems.size();
                        results.values = FilteredItems;
                    }
                } else {
                    synchronized (this) {
                        results.values = OriginalList;
                        results.count = OriginalList.size();
                    }

                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ItemList = (ArrayList<HashMap<String, Object>>) results.values;
                notifyDataSetChanged();

                MapServices.this.ServiceList.clear();

                //FilteredList = new ArrayList<HashMap<String, Object>>();
                //FilteredList.clear();
                for (int i = 0; i < ItemList.size(); i++) {
                    MapServices.this.ServiceList.add(ItemList.get(i));
                    notifyDataSetInvalidated();
                }

            }
        }
    }
}


