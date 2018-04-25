package com.example.project.mobilecapstone;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.project.mobilecapstone.Data.Room;
import com.example.project.mobilecapstone.Data.sharedData;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MapSearchActivity extends AppCompatActivity {

    MaterialSearchView searchView;

    String roomName = null;
    int buildingId = 0;
    ListView lstView;
    Room[] lstSource = null;
    String[] lstName = null;
    private static final String TAG = "MapSearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.SearchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search your Room ");
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        lstView = findViewById(R.id.lstView);
        new getRoomList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                //If closed Search View , lstView will return default
                lstView = (ListView) findViewById(R.id.lstView);
                ArrayAdapter adapter = new ArrayAdapter(MapSearchActivity.this, android.R.layout.simple_list_item_1, lstName);
                lstView.setAdapter(adapter);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                roomName = query;
                buildingId = 1;
                new getRoom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    List<String> lstFound = new ArrayList();
                    for (Room r : lstSource) {
                        if (r.getName().contains(newText))
                            lstFound.add(r.getName());
                    }

                    ArrayAdapter adapter = new ArrayAdapter(MapSearchActivity.this, android.R.layout.simple_list_item_1, lstFound);
                    lstView.setAdapter(adapter);
                } else {
                    //if search text is null
                    //return default
                    ArrayAdapter adapter = new ArrayAdapter(MapSearchActivity.this, android.R.layout.simple_list_item_1, lstName);
                    lstView.setAdapter(adapter);
                }
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_Search);
        searchView.setMenuItem(item);
        return true;
    }

    public class getRoom extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL("http://" + sharedData.IP + "/api/Room/searchRoom?name=" + roomName + "&buildingId=" + buildingId);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseOutput = new StringBuilder();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        responseOutput.append(line);
                    }
                    br.close();
                    String json = responseOutput.toString();
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "doInBackground: ", e);
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "doInBackground: ", e);
                e.printStackTrace();
            }
            return null;
        }
    }

    public class getRoomList extends AsyncTask<String, Void, String> {

        int buildingId = 1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://" + sharedData.IP + "/api/Room/GetListRoom?buildingId=" + buildingId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseOutput = new StringBuilder();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        responseOutput.append(line);
                    }
                    br.close();
                    String json = responseOutput.toString();
                    convertToRoomArray(json);
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(TAG, "doInBackground: ", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(TAG, "doInBackground: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            ArrayAdapter adapter = new ArrayAdapter(MapSearchActivity.this, android.R.layout.simple_list_item_1, lstName);
            lstView.setAdapter(adapter);
            lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.putExtra("Floor", lstSource[position].getFloor());
                    intent.putExtra("Width", lstSource[position].getWidth());
                    intent.putExtra("Length", lstSource[position].getLength());
                    setResult(1, intent);
                    MapSearchActivity.this.finish();
                    /*new getRoom().execute();*/
                }
            });
        }
    }

    public void convertToRoomArray(String json) {
        try {
            JSONArray list = new JSONArray(json);
            int total = list.length();
            lstSource = new Room[total];
            lstName = new String[total];
            for (int i = 0; i < total; i++) {
                JSONObject jsonObject = new JSONObject(list.get(i).toString());
                Room newRoom = new Room(jsonObject.getInt("Id"), jsonObject.getString("Name"),
                        jsonObject.getInt("Floor"), jsonObject.getDouble("Length"),
                        jsonObject.getDouble("Width"), jsonObject.getInt("MapId"),
                        jsonObject.getDouble("Longitude"), jsonObject.getDouble("Latitude"));
                lstSource[i] = newRoom;
                lstName[i] = jsonObject.getString("Name");
            }
        } catch (JSONException e) {
            Log.e(TAG, "convertToRoomArray: ", e);
            e.printStackTrace();
        }
    }
}
