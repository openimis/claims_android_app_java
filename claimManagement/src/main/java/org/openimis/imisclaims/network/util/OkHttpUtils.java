package org.openimis.imisclaims.network.util;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import org.openimis.imisclaims.BuildConfig;
import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.network.okhttp.AuthorizationInterceptor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpUtils {

    private static volatile OkHttpClient client = null;

    private OkHttpUtils() {
        throw new IllegalAccessError("This constructor is private");
    }

    @NonNull
    public static OkHttpClient getDefaultOkHttpClient() {
        if (client == null) {
            synchronized (OkHttpUtils.class) {
                if (client == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                    interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);
                    builder.addInterceptor(interceptor);
                    builder.addInterceptor(new AuthorizationInterceptor(Global.getGlobal()));
                    client = OkHttpUtils.ignoreSslCertificateInDebug(builder).build();
                }
            }
        }
        return client;
    }

    @SuppressLint({"CustomX509TrustManager", "TrustAllX509TrustManager"})
    @NonNull
    public static OkHttpClient.Builder ignoreSslCertificateInDebug(@NonNull OkHttpClient.Builder builder) {
        if (BuildConfig.DEBUG) {
            try {
                X509TrustManager trustManager = new X509TrustManager() {
                    @SuppressLint("")
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                };
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                builder.sslSocketFactory(
                        sslContext.getSocketFactory(),
                        trustManager);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return builder;
    }
}
