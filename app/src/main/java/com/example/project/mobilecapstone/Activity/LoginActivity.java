package com.example.project.mobilecapstone.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.mobilecapstone.Data.DeviceInfo;
import com.example.project.mobilecapstone.Data.sharedData;
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
    Button btn_ip;

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtUsername = findViewById(R.id.input_username);
        txtPassword = findViewById(R.id.input_password);

        btn_ip = LoginActivity.this.findViewById(R.id.btn_login);
        btn_ip.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Dialog dialog = new Dialog(LoginActivity.this);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_change_ip);
                //window manager to set dialog attributes
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.copyFrom(dialog.getWindow().getAttributes());
                params.width = WindowManager.LayoutParams.MATCH_PARENT;

                final EditText txtIP = dialog.findViewById(R.id.txtIP);
                Button btn_ip = dialog.findViewById(R.id.btn_set_ip);
                btn_ip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sharedData.IP = txtIP.getText().toString();
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        });
        btn_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginCheck();
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        sharedData.width = display.getWidth();
        sharedData.height = display.getHeight() - 240;
        checkPermissions();
        new CreatePosition(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Intent intent = new Intent(this, PositionService.class);
        startService(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void checkPermissions() {
        //check state permission
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            getImeiDevice();
            device.setIMEI(imeiDevice);
            Log.e(TAG, "DeviceIMEI: -" + imeiDevice + "---" + device.getIMEI());
        } else {
            requestPermission();
        }
//        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//        } else {
//            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 2);
//        }
//        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//        } else {
//            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 3);
//        }
//
//        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
//        } else {
//            requestPermission(Manifest.permission.INTERNET, 4);
//        }
//
//        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//        } else {
//            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 5);
//        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 1);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "thank you", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "Denied permission to use internet", Toast.LENGTH_SHORT);
            }
        }
    }

    public void onRegisterClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        LoginActivity.this.startActivity(intent);
    }

    public void LoginCheck() {
        //TODO: check login before switch activity
        //Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
        //LoginActivity.this.startActivity(intent);
        new CheckLogin().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getImeiDevice() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            imeiDevice = tm.getDeviceId();

        }
    }

    /*public void onChangeIPClick(View view) {

    }*/

    public class CheckLogin extends AsyncTask<String, Void, String> {
        int responseCode;
        StringBuilder responseOutput;

        @Override
        protected void onPreExecute() {

            username = txtUsername.getText().toString();
            password = txtPassword.getText().toString();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://" + sharedData.IP + "/api/User/Get?username=" + username + "&password=" + password);
                Log.e(TAG, "doInBackground: Login url" + sharedData.IP);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                responseCode = connection.getResponseCode();
                Log.e(TAG, "doInBackground: " + responseOutput);
                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + "GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                //logging
                Log.e(TAG, "doInBackground: CheckLogin" + responseOutput.toString());
                br.close();
                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground: CheckLogin" + e);
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: CheckLogin" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Toast.makeText(LoginActivity.this, "Xin chào!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("Username", username);
                    JSONObject obj = new JSONObject(responseOutput.toString());
                    intent.putExtra("Fullname", obj.getString("Fullname"));
                    intent.putExtra("Id", obj.getString("Id").toString());
                    intent.putExtra("packType", obj.getString("PackageId"));
                    startActivity(intent);
                    //prevent going back by press back button
                    finish();
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Toast.makeText(LoginActivity.this, "Tên hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                } else if (responseCode == 417) {
                    new AlertDialog.Builder(LoginActivity.this).setTitle("Tài khoản hết hạn").setMessage("Tài khoản này đã hết hạn bản quyền.\nBạn có muốn đến trang web để gia hạn tài khoản ?").setPositiveButton("Đến web", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + sharedData.IP + ":36110/"));
                            startActivity(browserIntent);
                        }
                    }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).create().show();
                    Toast.makeText(LoginActivity.this, "Tài khoản hết hạn sử dụng, vui lòng gia hạn bản quyền", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Log.e(TAG, "onPostExecute: LOGIN", e);
                e.printStackTrace();
            }
        }
    }
}
