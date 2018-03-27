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
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.project.mobilecapstone.Data.Corner;
import com.example.project.mobilecapstone.Data.Room;
import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.R;
import com.example.project.mobilecapstone.Utils.GPSRouter;
import com.example.project.mobilecapstone.Utils.Utils;

import org.json.JSONArray;
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

    public boolean stopTask = false;
    GPSRouter gps;
    static double latitude;
    static double longitude;
    static double altitude;
    static float posX = 0;
    static float posY = 0;
    static double deviceLat = 0;
    static double deviceLong = 0;
    static float devicePosX = 0;
    static float devicePosY = 0;
    private String currentFloor = "";
    public static Room[] rooms = null;
    public static Corner[] corners = null;

    static int width = 0;
    static int height = 0;
    private static final String TAG = "MapFragment";
    CanvasMapView canvasMapView;
//    private String isMoving;
//    private Sensor mySensor;
//    private SensorManager SM;
//    private float accelLast, accelCurrent, accel, x, y, z;
//    private float[] values;

    public MapFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        //create sensor manager
//        SM = (SensorManager) context.getSystemService(SENSOR_SERVICE);
//
//        //accelerometer sensor
//        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        //register sensor listener
//        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        this.getArguments();
        new CanvasAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new initListRoom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1, 1);
        new initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
        getActivity();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        //assign text view

//        accelLast = SM.GRAVITY_EARTH;
//        accelCurrent = SM.GRAVITY_EARTH;

        FrameLayout layout = v.findViewById(R.id.canvasView);
        canvasMapView = new CanvasMapView(getContext());
        canvasMapView.setId(R.id.viewCanvas);
        layout.addView(canvasMapView);
        stopTask = false;
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            values = sensorEvent.values.clone();
//            x = values[0];
//            y = values[1];
//            z = values[2];
//            accelLast = accelCurrent;
//            accelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
//            float delta = accelCurrent - accelLast;
//            accel = accel * 0.9f + delta;
//            // Make this higher or lower according to how much
//            // motion you want to detect
//            CanvasMapView canvasMapView = this.getView().findViewById(R.id.viewCanvas);
//            if (accel > 1) {
//                isMoving = "Moving";
//                if (gps.canGetLocation()) {
////                    latitude = gps.getLatitude();
////                    longitude = gps.getLongitude();
//                    latitude = 10.8530062;
//                    longitude = 106.6296201;
//                } else {
//                    gps.showSettingAlert();
//                }
//                getCurrentPointMap();
//                Log.e(TAG, "onSensorChanged: moving");
//                canvasMapView.invalidate();
//            } else {
////                canvasMapView.invalidate();
////                isMoving = "still";
//            }
//            Log.e(TAG, "onSensorChanged: still");
//        }
//    }

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }

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
            if (first) {
                first = false;
                new initListRoom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1, 1);
                new initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
            }
            //get location from GPSRouter class
            Context context = this.getContext();
            gps = new GPSRouter(context);
            if (gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                altitude = gps.getAltitude();
            latitude = 10.8529728;
            longitude = 106.6295536;
            } else {
                gps.showSettingAlert();
            }
            if (altitude <= 6.5) {
                map = BitmapFactory.decodeResource(getResources(), R.drawable.floorG1);
            } else if (altitude <= 12.3){
                map = BitmapFactory.decodeResource(getResources(), R.drawable.floor1);
            } else if (altitude <= 16.1){
                map = BitmapFactory.decodeResource(getResources(), R.drawable.floor2);
            }else if (altitude <= 19.39){
                map = BitmapFactory.decodeResource(getResources(), R.drawable.floor3);
            }else if (altitude <= 22.5){
                map = BitmapFactory.decodeResource(getResources(), R.drawable.floor4);
            }else {
                map = BitmapFactory.decodeResource(getResources(), R.drawable.floor5);
            }
            scaleMap = Bitmap.createScaledBitmap(map, width, height, false);

            canvas.drawBitmap(scaleMap, 0, 0, null);
            if (posX != 0 || posY != 0) {
                mPaint.setColor(Color.BLUE);
                canvas.drawCircle(posX, posY, 10, mPaint);
            }
            if (devicePosX != 0 || devicePosY != 0) {
                mPaint.setColor(Color.GREEN);
                canvas.drawCircle(devicePosX, devicePosY, 10, mPaint);
            }
            Toast.makeText(this.getContext(), "Your location is - \nLat: " +
                            latitude + "\nLong: " + longitude,
                    Toast.LENGTH_LONG).show();
        }

        private void initPaint() {
            mPaint.setColor(Color.BLUE);
            mPaint.setStrokeWidth(20);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND); // Cho dau cac duong ve duoc bo tron
            mPaint.setAlpha(150);
        }

    }

    public static void getPointMap(double latitude, double longitude, boolean isDevice) {
        int corner = 1;
        double min = Utils.PerpendicularDistance(corners[0], corners[1], longitude, latitude);
        double perpendicular = Utils.PerpendicularDistance(corners[1], corners[2], longitude, latitude);
        if (min > perpendicular) {
            min = perpendicular;
            corner = 2;
        }
        perpendicular = Utils.PerpendicularDistance(corners[2], corners[3], longitude, latitude);
        if (perpendicular < min) {
            min = perpendicular;
            corner = 3;
        }
        perpendicular = Utils.PerpendicularDistance(corners[3], corners[0], longitude, latitude);
        if (perpendicular < min) {
            min = perpendicular;
            corner = 4;
        }

        if (isDevice) {

        } else {
            Corner currentCorner1 = corners[1];
            Corner currentCorner2 = corners[0];

            if (corner == 1) {
                //29  18
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                posY = (float) (height / distanceCorner * temp);
//                currentCorner2 = corners[2];
//                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
//                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
//                temp = Utils.PerpendicularDistance(currentCorner1, currentCorner2, longitude, latitude);
//                temp = Utils.getPixelWithPer(temp, distance2);
//                posX = (float)(width / distanceCorner * temp);
                posX = width / 18 * ((float) (rooms[0].getWidth()));
            }
            if (corner == 3) {
                currentCorner1 = corners[2];
                currentCorner2 = corners[3];
                double distance2 = (float) Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);

                posX = width / 18 * ((float) (rooms[0].getWidth() * 5));
                posY = (float) (height / distanceCorner * temp);
            }
        }
//        for (Room room : rooms) {
//            double cal = Utils.HaversineInM(latitude, longitude, room.getLatitude(), room.getLongitude());
//            if (cal <= 2.5) {
//                if (isDevice == false) {
//                    posX = Utils.getPixel(width / 12, room.getPosAX(), room.getPosBX());
//                    posY = Utils.getPixel(height / 12, room.getPosAY(), room.getPosBY());
//                } else {
//                    devicePosX = Utils.getPixel(width / 12, room.getPosAX(), room.getPosBX());
//                    devicePosY = Utils.getPixel(height / 12, room.getPosAY(), room.getPosBY());
//                }
//            }
//        }
    }


    public class initListRoom extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                int mapId = Integer.parseInt(objects[0].toString());
                int floor = Integer.parseInt(objects[1].toString());
                URL url = new URL("http://" + sharedData.IP + ":57305/api/Room/GetListRoom?floor=" + floor + "&mapId=" + mapId);
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
                    try {
                        JSONArray list = new JSONArray(json);
                        int total = list.length();
                        rooms = new Room[total];
                        for (int i = 0; i < total; i++) {
                            JSONObject jsonObject = new JSONObject(list.get(i).toString());
                            Room newRoom = new Room(jsonObject.getInt("Id"), jsonObject.getString("Name"),
                                    jsonObject.getInt("Floor"), jsonObject.getDouble("Length"),
                                    jsonObject.getDouble("Width"), jsonObject.getInt("MapId"),
                                    jsonObject.getDouble("Longitude"), jsonObject.getDouble("Latitude"),
                                    jsonObject.getInt("PosAX"), jsonObject.getInt("PosAY"),
                                    jsonObject.getInt("PosBX"), jsonObject.getInt("PosBY"));
                            rooms[i] = newRoom;
                        }
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

    public class initListCorner extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                int mapId = Integer.parseInt(objects[0].toString());
                URL url = new URL("http://" + sharedData.IP + ":57305/api/Corner/GetAllCornerWithMap?&mapId=" + mapId);
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
                    try {
                        JSONArray list = new JSONArray(json);
                        int total = list.length();
                        corners = new Corner[total];
                        for (int i = 0; i < total; i++) {
                            JSONObject jsonObject = new JSONObject(list.get(i).toString());
                            Corner newCorner = new Corner(jsonObject.getInt("MapId"), jsonObject.getString("Description"),
                                    jsonObject.getDouble("Longitude"), jsonObject.getDouble("Latitude"),
                                    jsonObject.getInt("Id"), jsonObject.getInt("Floor"), jsonObject.getInt("Position"));
                            corners[i] = newCorner;
                        }
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

    public class CanvasAsyncTask extends AsyncTask<Void, Double, Void> {

        private MapFragment fragment;

        public CanvasAsyncTask(MapFragment fragment) {
            this.fragment = fragment;
        }
//        private Activity activity;
//        public CanvasAsyTask(Activity activity) {
//            this.activity = activity;
//        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (stopTask == false) {
                SystemClock.sleep(6000);
                Bundle bundle = fragment.getArguments();
                if (bundle != null) {
                    deviceLat = bundle.getDouble("LAT");
                    deviceLong = bundle.getDouble("LONG");
                }

                getPointMap(latitude, longitude, false);
                if (deviceLat != 0 || deviceLong != 0) {
                    getPointMap(deviceLat, deviceLong, true);
                }
                publishProgress();
            }
            return null;
        }

        @SuppressLint("WrongCall")
        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
//            CanvasMapView canvasView = activity.findViewById(R.id.canvasView);
            canvasMapView.invalidate();
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);
        }

    }
}
