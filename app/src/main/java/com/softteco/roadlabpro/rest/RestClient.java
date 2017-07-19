package com.softteco.roadlabpro.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import com.softteco.roadlabpro.util.Constants;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okio.Buffer;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class RestClient {

    private static final String TAG = RestClient.class.getSimpleName();

    private static RestClient restClient;
    private Retrofit retrofit;

    public static RestClient getInstance() {
        if (restClient == null) {
            restClient = new RestClient();
        }
        return restClient;
    }

    public ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }

    private Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                            return new Date(json.getAsJsonPrimitive().getAsLong());
                        }
                    })
                    .setPrettyPrinting()
                    .create();

            retrofit = new Retrofit.Builder()
                    .client(getUnsafeOkHttpClient())
                    .baseUrl(Constants.GOOGLE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            retrofit.client().interceptors().add(new RequestInterceptor());
        }
        return retrofit;
    }

    private class RequestInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            try {
                Log.i(TAG, "request is: \n" + request.toString());
                Log.i(TAG, "request headers are: \n" + request.headers().toString());
                Buffer buffer = new Buffer();
                if (request.body() != null) {
                    request.body().writeTo(buffer);
                }
                String bodyStr = buffer.readUtf8();
                Log.i(TAG, "REQUEST body is: \n" + bodyStr);
                response = chain.proceed(request);
                String responseBodyString = "";
                MediaType type = null;
                if (response.body() != null) {
                    type = response.body().contentType();
                    responseBodyString = response.body().string();
                }
                response = response.newBuilder().body(ResponseBody.create(type, responseBodyString.getBytes())).build();
                Log.i(TAG, "RESPONSE body is \n" + responseBodyString);
                return response;
            } catch (Exception e) {
                Log.e(TAG, "RequestInterceptor: intercept", e);
            }
            return response;
        }
    }
    
    public OkHttpClient getUnsafeOkHttpClient() {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(getSslSocketFactory());
            okHttpClient.setHostnameVerifier(new NullHostNameVerifier());
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static javax.net.ssl.SSLSocketFactory getSslSocketFactory() {
        SSLContext sslContext = null;
        try {
            // Create an SSLContext that uses our TrustManager
            sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = {new MyTrustManager()};
            sslContext.init(null, trustManagers, null);
            sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        if (sslContext != null) {
            return sslContext.getSocketFactory();
        }
        return null;
    }

    private static class MyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }
}
