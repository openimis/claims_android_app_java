package com.exact.imis.claim;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.exact.imis.claim.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AddItems extends AppCompatActivity {
	
//	Spinner spItems;
	ListView lvItems;
	TextView tvCode,tvName;
	EditText etQuantity, etAmount;
	Button btnAdd;
	SQLiteDatabase db;
    AutoCompleteTextView etItems;

	
	int Pos;
	
	ArrayList<HashMap<String, String>> ItemList = new ArrayList<HashMap<String,String>>();
	//ArrayList<HashMap<String,String>> lvItemList;
	
	HashMap<String, String> oItem;
	SimpleAdapter alAdapter;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.additems);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getResources().getString(R.string.app_name_claim));
		actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    
//	    spItems = (Spinner)findViewById(R.id.spItems);
	    lvItems = (ListView)findViewById(R.id.lvItems);
	    tvCode = (TextView)findViewById(R.id.tvCode);
	    tvName =  (TextView)findViewById(R.id.tvName);
	    //tvPrice = (TextView)findViewById(R.id.tvPrice);
	    etQuantity = (EditText)findViewById(R.id.etQuantity);
	    etAmount = (EditText)findViewById(R.id.etAmount);
        etItems  = (AutoCompleteTextView)findViewById(R.id.etItems);
	    
	    //ClaimActivity.lvItemList = new ArrayList<HashMap<String, String>>();

	    alAdapter = new SimpleAdapter(AddItems.this,ClaimActivity.lvItemList, R.layout.lvitem,
				new String[]{"Code","Name","Price","Quantity"},
				new int[]{R.id.tvLvCode, R.id.tvLvName, R.id.tvLvPrice, R.id.tvLvQuantity});


        ItemAdapter itemAdapter = new ItemAdapter(AddItems.this,null);
        etItems.setAdapter(itemAdapter);
        etItems.setThreshold(1);

        etItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {

                if (position >= 0){

                    Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                    final int itemColumnIndex = cursor.getColumnIndexOrThrow("Code");
                    final int descColumnIndex = cursor.getColumnIndexOrThrow("Name");
                    String Code = cursor.getString(itemColumnIndex);
                    String Name = cursor.getString(descColumnIndex);

					oItem = new HashMap<String, String>();
                    oItem.put("Code",Code);
                    oItem.put("Name",Name);


//					etAmount.setText(oItem.get("Price"));
					etQuantity.setText("1");
					etAmount.setText("0");
				}

            }
        });
	  
	    lvItems.setAdapter(alAdapter);
	    
	    btnAdd = (Button)findViewById(R.id.btnAdd);
	   
	    btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {

					if (oItem == null) return;

                    String Amount,Quantity = "1";

					HashMap<String,String> lvItem = new HashMap<String,String>();
					lvItem.put("Code", oItem.get("Code"));
					lvItem.put("Name",oItem.get("Name"));
					Amount = etAmount.getText().toString(); 
					lvItem.put("Price", Amount);
					if(etQuantity.getText().toString().length() == 0) Quantity = "1"; else Quantity = etQuantity.getText().toString();
					lvItem.put("Quantity", Quantity);
					ClaimActivity.lvItemList.add(lvItem);
					
					alAdapter.notifyDataSetChanged();

                    etItems.setText("");
                    etAmount.setText("");
                    etQuantity.setText("");

					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d("AddLvError", e.getMessage());
				}
			}
		});

	    
	    //BindSpItems();



	    
//	   spItems.setOnItemSelectedListener(new SpinnerOnItemSelected() {
//
//		@Override
//		public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
//
//			try {
//				if (position >= 0){
//					oItem = (HashMap<String, String>)parent.getItemAtPosition(position);
//
//					etAmount.setText(oItem.get("Price"));
//					etQuantity.setText("1");
//					etAmount.setText("0");
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				Log.d("onSelectedError", e.getMessage());
//			}
//
//		}
//
//		@Override
//		public void onNothingSelected(AdapterView<?> arg0) {
//			// TODO Auto-generated method stub
//
//		}
//	});
	    
	lvItems.setOnItemLongClickListener(new onItemLongClickListener() {
		@Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
          try {

        	  Pos = position;
        	  HideAllDeleteButtons();

        	  	Button d = (Button)view.findViewById(R.id.btnDelete);
        	  	d.setVisibility(View.VISIBLE);

        	  	d.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ClaimActivity.lvItemList.remove(Pos);
						HideAllDeleteButtons();
//						alAdapter.notifyDataSetChanged();
					}
				});


		} catch (Exception e) {
			Log.d("ErrorOnLongClick", e.getMessage());
		}
		return true;
        }
	});
	   
	
	
	}
	
	private void HideAllDeleteButtons(){
		for(int i=0;i<=lvItems.getLastVisiblePosition();i++){
	  		Button Delete = (Button)lvItems.getChildAt(i).findViewById(R.id.btnDelete);
	  		Delete.setVisibility(View.GONE);
	  }
	}
	
//	private void BindSpItems(){
//		//List<String> Items = new ArrayList<String>();
//
//		SQLHandler sql = new SQLHandler(this);
//		String Table = "tblMapping";
//		String Columns[] = {"Code","Name","Type"};
//		String Criteria = "Type='I'";
//
//		//db = openOrCreateDatabase(ClaimActivity.Path + "ImisData.db3", SQLiteDatabase.OPEN_READONLY, null);
//
//		Cursor c = sql.getData(Table, Columns, Criteria);
//
//		//Cursor c = db.query(Table, Columns, Criteria, null, null, null, null);
//
//if (c.getCount()==0 || c == null){
//
//			new AlertDialog.Builder(this)
//			.setMessage(getResources().getString(R.string.MappedItemMissing))
//			.setCancelable(false)
//			.setTitle(getResources().getString(R.string.NoItemMapped))
//			.setPositiveButton(getResources().getString(R.string.Ok), new android.content.DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					finish();
//				}
//			}).create().show();
//
//		}
//
//
//		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
//			//Items.add(c.getString(0));
//			HashMap<String, String> Item = new HashMap<String, String>();
//
//			Item.put("Code", c.getString(0));
//			Item.put("Name", c.getString(1));
//			//Item.put("Price", c.getString(3));
//			ItemList.add(Item);
//		}
//
//		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinneritem, R.id.tvCode);
//
//
//		SimpleAdapter adapter = new SimpleAdapter(AddItems.this,ItemList,R.layout.spinneritem,
//				new String[]{"Code","Name"},
//				new int[]{R.id.tvCode,R.id.tvName});
//
//		try {
//			spItems.setAdapter(adapter);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
public boolean onOptionsItemSelected(MenuItem item){
	onBackPressed();
//	Intent myIntent = new Intent(getApplicationContext(), ClaimActivity.class);
//	startActivityForResult(myIntent, 0);
	//finish();
	return true;
}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}



}
