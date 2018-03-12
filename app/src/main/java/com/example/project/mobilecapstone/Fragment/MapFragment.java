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

import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.R;
import com.example.project.mobilecapstone.Utils.GPSRouter;
import com.example.project.mobilecapstone.Utils.Utils;

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
    static float posX = 0;
    static float posY = 0;
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
        //get location from GPSRouter class
        Context context = getContext();
        gps = new GPSRouter(context);
        if (gps.canGetLocation()) {
//            latitude = 10.8530167;
//            longitude = 106.6296201;
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {
            gps.showSettingAlert();
        }
//        //create sensor manager
//        SM = (SensorManager) context.getSystemService(SENSOR_SERVICE);
//
//        //accelerometer sensor
//        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        //register sensor listener
//        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
            this.getArguments();
        new CanvasAsyTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            if (posX != 0 || posY != 0) {
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

    public static void getCurrentPointMap() {
        try {
            URL url = new URL("http://" + sharedData.IP + ":57305/api/Position/CalculatePosition?floor=" + 1 + "&mapId=" + 1
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
                    posX = Utils.getPixel(width / 12, obj.getInt("PosAX"), obj.getInt("PosBX"));
                    posY = Utils.getPixel(height / 12, obj.getInt("PosAY"), obj.getInt("PosBY"));
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
    }

    public class CanvasAsyTask extends AsyncTask<Void, Double, Void> {

        private Fragment fragment;

        public CanvasAsyTask(Fragment fragment) {
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
            while (true) {
                Bundle bundle = fragment.getArguments();
                SystemClock.sleep(3000);
                if (gps.canGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
//                    latitude = 10.8529373;
//                    longitude = 106.6294958;
                } else {
                    gps.showSettingAlert();
                }
                getCurrentPointMap();
                publishProgress();
            }
//            return null;
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
