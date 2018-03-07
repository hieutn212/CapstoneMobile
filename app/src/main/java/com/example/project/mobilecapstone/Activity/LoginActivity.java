package com.example.project.mobilecapstone.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.mobilecapstone.Data.DeviceInfo;
import com.example.project.mobilecapstone.R;
import com.example.project.mobilecapstone.Utils.CreatePosition;
import com.example.project.mobilecapstone.Utils.GPSRouter;
import com.example.project.mobilecapstone.Utils.PositionService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText txtUsername;
    EditText txtPassword;
    String username, password;
    String imeiDevice;
    DeviceInfo device = new DeviceInfo();
    private static final String TAG = "LoginActivity";
    private GPSRouter gps;

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = findViewById(R.id.input_username);
        txtPassword = findViewById(R.id.input_password);
        //check state permission
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            getImeiDevice();
            device.setIMEI(imeiDevice);
            Log.e(TAG, "DeviceIMEI: -" + imeiDevice +"---" + device.getIMEI());
        }
        else {
            requestReadPhoneStatePermission();
        }
        new CreatePosition().execute();
        Intent intent = new Intent(this, PositionService.class);
        startService(intent);
    }

    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            new AlertDialog.Builder(this).setTitle("Permission needed")
                    .setMessage("This permission is needed for the functions to work")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 3){
            if (grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"thank you",Toast.LENGTH_SHORT);
            }else{
                Toast.makeText(this,"Denied permission to use internet",Toast.LENGTH_SHORT);
            }
        }
    }

    public void onRegisterClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        LoginActivity.this.startActivity(intent);
    }

    public void onLoginClick(View view) {
        //TODO: check login before switch activity
        //Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        //LoginActivity.this.startActivity(intent);
        new CheckLogin().execute();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getImeiDevice(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            imeiDevice = tm.getDeviceId();

        }
    }

    public class CheckLogin extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            username = txtUsername.getText().toString();
            password = txtPassword.getText().toString();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://192.168.1.103:57305/api/User/Get?username=" + username + "&password=" + password);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                //arr.add("" + responseCode+ "a");
                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + "GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                final StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                //logging
                Log.e(TAG, "doInBackground: " + responseOutput.toString());
                br.close();
                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Xin chào!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("Username", username);
                    intent.putExtra("Fullname", new JSONObject(responseOutput.toString()).getString("Fullname").toString());
                    intent.putExtra("Id", new JSONObject(responseOutput.toString()).getString("Id").toString());
                    startActivity(intent);
                }
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Không hợp lệ!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //Toast.makeText(LoginActivity.this, s, Toast.LENGTH_LONG).show();

        }
    }


}
