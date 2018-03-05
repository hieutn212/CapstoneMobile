package com.example.project.mobilecapstone.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.project.mobilecapstone.Data.DeviceInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by admin on 3/4/2018.
 */

public class CreatePosition extends AsyncTask<String, Void, String> {

    private Context context;
    private GPSRouter gps;
    DeviceInfo device = new DeviceInfo();
    private static final String TAG = "CreatePosition";

    @Override
    protected void onPreExecute() {
        gps = new GPSRouter(context);
    }

    @Override
    protected String doInBackground(String... strings) {
        try {

            Log.e(TAG, "doInBackground:" + gps.getLatitude());
            URL url = new URL("http://192.168.1.6:57305/api/Position/CreateProductPosition?latitude=" + gps.getLatitude() + "&longitude=" + gps.getLongitude() + "&altitude=" + gps.getAltitude() + "&deviceId=" + device.getIMEI()); //
            Log.e(TAG, "doInBackground:" + url.toString());
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("latitude", gps.getLatitude());
            postDataParams.put("longitude", gps.getLongitude());
            postDataParams.put("altitude", gps.getAltitude());
            postDataParams.put("deviceId", device.getIMEI());
            Log.e("params", postDataParams.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(
                        conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line = "";

                while ((line = in.readLine()) != null) {

                    sb.append(line);
                    break;
                }

                in.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: Register Activity", e);
        }
        return null;
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
