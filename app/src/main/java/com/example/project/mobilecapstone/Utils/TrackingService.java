package com.example.project.mobilecapstone.Utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.project.mobilecapstone.Data.sharedData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * Created by admin on 3/9/2018.
 */

public class TrackingService extends IntentService {

    private StringBuilder responseOutput;
    private String deviceId;

    public TrackingService(){
        super("TrackingService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //TODO:get extra from intent
        deviceId = intent.getStringExtra("id");
        while (true) {
            try {
                //get position data from server
                Thread.sleep(5000);
                URL url = new URL("http://"+ sharedData.IP +":57305/api/Position/trackingProduct?deviceId=" + deviceId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                //send result to MapFragment
                String device = responseOutput.toString();
                sharedData.LAT = new JSONObject(device).getDouble("Latitude");
                sharedData.LONG = new JSONObject(device).getDouble("Longitude");
                sharedData.ALT = new JSONObject(device).getDouble("Altitude");
                Log.e(TAG, "onHandleIntent: TrackingService-Location"+ device);
                Log.e(TAG, "onHandleIntent: TrackingService-sharedData"+ sharedData.LAT+"---"+sharedData.LONG+"---"+sharedData.ALT);
            } catch (MalformedURLException e) {
                Log.e(TAG, "onHandleIntent: TrackingService --- ",e );
            } catch (IOException e) {
                Log.e(TAG, "onHandleIntent: TrackingService --- ",e );
            } catch (InterruptedException e) {
                Log.e(TAG, "onHandleIntent: TrackingService --- ",e );
            } catch (JSONException e) {
                Log.e(TAG, "onHandleIntent: TrackingService --- ",e );
            }
        }
    }
}
