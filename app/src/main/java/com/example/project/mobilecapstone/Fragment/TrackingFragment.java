package com.example.project.mobilecapstone.Fragment;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.mobilecapstone.Data.UserInfo;
import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

public class TrackingFragment extends Fragment {

    Button btn_createDevice;
    ArrayList<String> arr = new ArrayList<String>();
    private String IMEI = "";
    private String name = "";
    private Dialog dialog;
    private int userId = Integer.parseInt(UserInfo.Id);
    ListView listview;
    TrackerListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tracking, container, false);
        //return inflater.inflate(R.layout.fragment_tracking, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new GetListDevice().execute();
        listview = getView().findViewById(R.id.device_list);
        adapter = new TrackerListAdapter();
        //add a device form button and handler
        btn_createDevice = getView().findViewById(R.id.btn_create_device);
        btn_createDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(getActivity());
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_add_device);
                //window manager to set dialog attributes
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.copyFrom(dialog.getWindow().getAttributes());
                params.width = WindowManager.LayoutParams.MATCH_PARENT;

                final EditText txtIMEI = dialog.findViewById(R.id.txtIMEI);
                final EditText txtName = dialog.findViewById(R.id.txtName);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);
                Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Get data from input fields
                        IMEI = txtIMEI.getText().toString();
                        name = txtName.getText().toString();
                        //create data string to send to server
                        try {
                            String data = URLEncoder.encode("IMEI", "UTF-8") + "=" + URLEncoder.encode(IMEI, "UTF-8");
                            data += URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
                            //send data to request handler method
                            new CreateProduct().execute(IMEI, name); // test thu
                            Log.e(TAG, "onClick: " + IMEI + name);
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, "onClick: Unsupported Encoding Exception", e);
                        } catch (IOException e) {
                            Log.e(TAG, "onClick: IO Exception", e);
                        }
                        Toast.makeText(getContext(), "request sent", Toast.LENGTH_SHORT);
                        dialog.cancel();
                    }
                });
                dialog.show();
                dialog.getWindow().setAttributes(params);
            }
        });
    }

    class TrackerListAdapter extends BaseAdapter implements ListAdapter {

        @Override
        public int getCount() {
            return arr.size();
        }

        @Override
        public Object getItem(int i) {
            return arr.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View v = view;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.device_list_item, null);
            }
            //handle textview and display string from list
            TextView deviceName = v.findViewById(R.id.device_name);
            final TextView deviceId = v.findViewById(R.id.device_id);
            try {
                deviceName.setText(new JSONObject(arr.get(i)).getString("Name"));
                deviceId.setText(new JSONObject(arr.get(i)).getString("Id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Handle buttons and add onCLickListener
            Button btn_track = v.findViewById(R.id.btn_locate);

            btn_track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(getContext(), TrackingService.class);
//                    intent.putExtra("id",deviceId.getText());
//                    getActivity().startService(intent);
                    new getLocation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, deviceId.getText().toString());
                }
            });
            return v;
        }
    }


    //create new device
    public class CreateProduct extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL("http://" + sharedData.IP +":57305/api/device/CreateProduct?IMEI=" + IMEI + "&name=" + name + "&userId=" + userId); // here is your URL path
                Log.e(TAG, "doInBackground-CreateProduct:" + url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
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
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    //get list device of user
    private class GetListDevice extends AsyncTask<String, Void, Void> {
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL("http://" + sharedData.IP +":57305/api/device/getListProduct?userId=" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                try {
                    convertToArray(responseOutput.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "doInBackground-getListDevice: " + arr.toString() + "-" + responseCode);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listview.setAdapter(adapter);
        }
    }

    //convert json object to array
    public void convertToArray(String s) throws JSONException {
        JSONArray array = new JSONArray(s);
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                arr.add(array.get(i).toString());
            }
        }
    }

    //get location of chosen product
    public class getLocation extends AsyncTask<String, Void, String> {

        StringBuilder responseOutput;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://"+ sharedData.IP +":57305/api/Position/trackingProduct?deviceId=" + params[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                int responseCode = connection.getResponseCode();
                Log.e(TAG, "doInBackground:" + responseCode);

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                Log.e(TAG, "doInBackground-getLocation: " + responseOutput.toString());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                String device = responseOutput.toString();
                double latitude = new JSONObject(device).getDouble("Latitude");
                double longitude = new JSONObject(device).getDouble("Longitude");
//                longitude = 106.6296557;
//                latitude = 10.8530122;
                Log.e(TAG, "onPostExecute: " + latitude + longitude);
                Bundle bundle = new Bundle();
                bundle.putDouble("LAT", latitude);
                bundle.putDouble("LONG", longitude);

                android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                MapFragment map = new MapFragment();
                map.setArguments(bundle);
                transaction.replace(R.id.content_main, map).commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
