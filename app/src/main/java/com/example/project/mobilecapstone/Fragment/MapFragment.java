package com.example.project.mobilecapstone.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.project.mobilecapstone.Data.CalculatorModel;
import com.example.project.mobilecapstone.R;
import com.example.project.mobilecapstone.Utils.GPSRouter;
import com.example.project.mobilecapstone.Utils.Utils;
import com.google.android.gms.maps.GoogleMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class MapFragment extends Fragment {

    private GoogleMap map;
    GPSRouter gps;
    double latitude;
    double longitude;
    CalculatorModel calObj = new CalculatorModel();
    float posX = 0;
    float posY = 0;
    int width = 0;
    int height = 0;
    private static final String TAG = "MapFragment";

    public MapFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //get location from GPSRouter class
        gps = new GPSRouter(getContext());
        if (gps.canGetLocation()) {
            latitude = 10.8530167;
//            latitude = gps.getLatitude();
//            longitude = gps.getLongitude();
            longitude = 106.6296201;
        } else {
            gps.showSettingAlert();
        }

        new CanvasAsyTask().execute();
        getActivity();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        FrameLayout layout = v.findViewById(R.id.canvasView);
        CanvasMapView canvasMapView = new CanvasMapView(getContext());
        layout.addView(canvasMapView);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //create View with Canvas for map
    private class CanvasMapView extends View {
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap map = BitmapFactory.decodeResource(getResources(), R.drawable.floor1);
        boolean first = true;

        public CanvasMapView(Context context) {
            super(context);
            initPaint();
        }

        public CanvasMapView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initPaint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            height = getHeight();
            width = getWidth();
            Bitmap scaleMap = Bitmap.createScaledBitmap(map, width, height, false);
            canvas.drawBitmap(scaleMap, 0, 0, null);
            if(posX != 0 || posY != 0){
            canvas.drawCircle(posX, posY, 10, mPaint);
            }
        }

        private void initPaint() {
            mPaint.setColor(Color.BLUE);
            mPaint.setStrokeWidth(20);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND); // Cho dau cac duong ve duoc bo tron
            mPaint.setAlpha(150);
        }
    }

    public class CanvasAsyTask extends AsyncTask<Void, Double, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
//
            try {
                URL url = new URL("http://192.168.1.103:57305/api/Position/CalculatePosition?floor=" + 1 + "&mapId=" + 1
                        + "&latitude=" + latitude + "&longitude=" + longitude);

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
                        JSONObject obj = new JSONObject(json).getJSONObject("Room");
                        posX = Utils.getPixel(width,obj.getInt("PosAX"),obj.getInt("PosBX"));
                        posY = Utils.getPixel(width,obj.getInt("PosAY"),obj.getInt("PosBY"));
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

        @SuppressLint("WrongCall")
        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);
        }

    }
}
