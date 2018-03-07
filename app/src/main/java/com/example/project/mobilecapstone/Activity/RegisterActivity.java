package com.example.project.mobilecapstone.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

public class RegisterActivity extends AppCompatActivity {

    EditText txtUsername;
    EditText txtPassword;
    EditText txtFullname;
    EditText txtDoB;
    Button btnDate, btnRegister;
    String username, password, fullname, DoB;
    private DatePickerDialog.OnDateSetListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtUsername = findViewById(R.id.input_username);
        txtPassword = findViewById(R.id.input_password);
        txtFullname = findViewById(R.id.input_fullname);
        txtDoB = findViewById(R.id.input_DoB);
        btnDate = findViewById(R.id.btn_date_pick);
        btnRegister = findViewById(R.id.btn_signup);
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this, listener, year, month, day);
                dialog.show();
            }
        });
        listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;
                String date = year + "-" + month + "-" + day;
                txtDoB.setText(date);
            }
        };

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CreateAccount().execute();
            }
        });
    }


    public void onLoginClick(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        RegisterActivity.this.startActivity(intent);

    }

    public class CreateAccount extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            username = String.valueOf(txtUsername.getText());
            password = String.valueOf(txtPassword.getText());
            fullname = String.valueOf(txtFullname.getText());
            DoB = String.valueOf(txtDoB.getText());
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL("http://" + sharedData.IP+":57305/api/User/CreateAccount?username="+username+"&password="+password+"&roleId=2&fullname="+fullname+"&birthday="+DoB+"&active=true"); //
                Log.e(TAG, "doInBackground:" + url.toString());
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("Username", username);
                postDataParams.put("Password", password);
                postDataParams.put("RoleId", "2");
                postDataParams.put("fullname", fullname);
                postDataParams.put("Birthday", DoB);
                postDataParams.put("Active", "true");
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

                    in.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this,"Tạo tài khoản thành công, mời đăng nhập lại",Toast.LENGTH_LONG);
                        }
                    });
                    Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                    startActivity(intent);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this,"Tạo tài khoản thất bại",Toast.LENGTH_LONG);
                        }
                    });
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


}
