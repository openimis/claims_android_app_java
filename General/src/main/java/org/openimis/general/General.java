package org.openimis.general;


import java.io.IOException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;

public class General {

    private static String _Domain = "http://demo.openimis.org/rest/";

    private static final String DEFAULT_RAR_PASSWORD = ")(#$1HsD";

	public String getDomain(){
		return _Domain;
	}

	public static String getDefaultRarPassword() {
		return DEFAULT_RAR_PASSWORD;
	}

	public int isSDCardAvailable(){
		String State = Environment.getExternalStorageState();
		if (State.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
			return 0;
		}else if(!State.equals(Environment.MEDIA_MOUNTED)){
			return -1;
		}else{
			return 1;
		}
	}

	public boolean isNetworkAvailable(Context ctx){
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		return (ni != null && ni.isConnected());

	}

	public void ChangeLanguage(Context ctx,String Language){
		Resources res = ctx.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration config = res.getConfiguration();
		config.locale = new Locale(Language.toLowerCase());
		res.updateConfiguration(config, dm);
	}

	public String getVersion(Context ctx, String PackageName){
		String VersionName = "";

		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(PackageName, 0);
			//int Code = info.versionCode;
			VersionName = info.versionName;


		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
		}
		return VersionName;

	}

	public String getFromRestApi(final String functionName) {
		String uri = getDomain()+ "api/";

		final String[] content = {null};
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(uri+functionName);
		httpGet.setHeader("Content-type", "application/json");

		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity respEntity = response.getEntity();
		if (respEntity != null) {
			try {
				content[0] = EntityUtils.toString(respEntity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content[0];
	}

	public boolean isNewVersionAvailable(String Field,Context ctx, String PackageName){
		String result = getFromRestApi("system/apkversion/" + Field);
		if(result.contains(",")) {
			result = result.replaceAll("(\\d+)\\,(\\d+)", "$1.$2");
		}
		return result == ""?false:Float.parseFloat(this.getVersion(ctx, PackageName).toString()) < Float.parseFloat(result);
	}
}
	
	

