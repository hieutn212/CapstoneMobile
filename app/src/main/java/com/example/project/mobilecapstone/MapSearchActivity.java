package com.example.project.mobilecapstone;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;

import com.example.project.mobilecapstone.Data.RoomModel;
import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.Utils.Utils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

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
    String[] lstSource = {

            "101",
            "103",
            "105",
            "107",
            "109",
            "111",
            "113",
            "115",
            "102",
            "Thư viện",
            "116"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);

        Toolbar toolbar = (Toolbar)findViewById(R.id.SearchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search your Room ");
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        lstView = (ListView) findViewById(R.id.lstView);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lstSource);
        lstView.setAdapter(adapter);

        searchView = (MaterialSearchView)findViewById(R.id.search_view);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                //If closed Search View , lstView will return default
                lstView = (ListView)findViewById(R.id.lstView);
                ArrayAdapter adapter = new ArrayAdapter(MapSearchActivity.this,android.R.layout.simple_list_item_1,lstSource);
                lstView.setAdapter(adapter);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                roomName = query;
                buildingId = 1;
                new getRoom().execute();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){
                    List<String> lstFound = new ArrayList<String>();
                    for(String item:lstSource){
                        if(item.contains(newText))
                            lstFound.add(item);
                    }

                    ArrayAdapter adapter = new ArrayAdapter(MapSearchActivity.this,android.R.layout.simple_list_item_1,lstFound);
                    lstView.setAdapter(adapter);
                }
                else{
                    //if search text is null
                    //return default
                    ArrayAdapter adapter = new ArrayAdapter(MapSearchActivity.this,android.R.layout.simple_list_item_1,lstSource);
                    lstView.setAdapter(adapter);
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search,menu);
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
            URL url = new URL("http://" + sharedData.IP + ":57305/api/Room/searchRoom?name=" + roomName + "&buildingId=" + buildingId);

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
                try {
                    JSONObject obj = new JSONObject(json);
                    Intent intent = new Intent();
                    intent.putExtra("PosAX", obj.getInt("PosAX"));
                    intent.putExtra("PosAY", obj.getInt("PosAY"));
                    intent.putExtra("PosBX", obj.getInt("PosBX"));
                    intent.putExtra("PosBY", obj.getInt("PosBY"));
                    setResult(1, intent);
                    MapSearchActivity.this.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
}
