package com.example.project.mobilecapstone.Fragment;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.project.mobilecapstone.Activity.HomeActivity;
import com.example.project.mobilecapstone.Data.Corner;
import com.example.project.mobilecapstone.Data.DirectionPoint;
import com.example.project.mobilecapstone.Data.Marker;
import com.example.project.mobilecapstone.Data.Room;
import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.R;
import com.example.project.mobilecapstone.Utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MapSearchRoomFragment extends Fragment {
    public boolean stopTask = false;
    static float posX = 0;
    static float posY = 0;
    static float devicePosX = 0;
    static float devicePosY = 0;
    private int currentFloor = 0;
    public static Room[] rooms = null;
    public static Corner[] corners = null;
    int mapId = 0;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ArrayList<String> listMap = new ArrayList<String>();
    Integer buildingId;
    private DownloadManager downloadManager;
    public SwipeRefreshLayout swipeRefreshLayout;
    static int width = 0;
    static int height = 0;
    float widthMap = 0;
    float lengthMap = 0;
    private static final String TAG = "MapTrackingFragment";
    MapSearchRoomFragment.CanvasMapView canvasMapView;
    FloatingActionButton floatingDirectionButton;
    FloatingActionButton floatingSwitchFloor;
    boolean navigate = false;
    static Marker[] markers = new Marker[6];
    static List<DirectionPoint> directionPoints = new ArrayList<>();
    View v;
    private static final int REQUEST_CODE_ROOM = 0x9345;
    android.support.v4.app.Fragment fragment;
    FragmentManager fragmentManager;
    private String result = "";
    static int stairsGo = -1;
//    private String isMoving;
//    private Sensor mySensor;
//    private SensorManager SM;
//    private float accelLast, accelCurrent, accel, x, y, z;
//    private float[] values;

    public MapSearchRoomFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        sharedPreference = getActivity().getSharedPreferences("ROOM_CORNER_INFO", getActivity().MODE_PRIVATE);
        editor = sharedPreference.edit();
        fragmentManager = getActivity().getSupportFragmentManager();
        new MapSearchRoomFragment.GetListMap().execute();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            posX = bundle.getFloat("PosX");
            posY = bundle.getFloat("PosY");
            widthMap = bundle.getFloat("WidthMap");
            lengthMap = bundle.getFloat("LengthMap");
            currentFloor = bundle.getInt("Floor");
            stairsGo = bundle.getInt("Stairs");
            navigate = bundle.getBoolean("navigate");
        }
        fragment = this;
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_map_search_room, container, false);
        floatingDirectionButton = getActivity().findViewById(R.id.navigation);
        floatingSwitchFloor = getActivity().findViewById(R.id.switch_floor);
        floatingDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (navigate) {
                    navigate = false;
                    directionPoints.clear();
                    canvasMapView.invalidate();
                    floatingDirectionButton.setImageResource(R.drawable.ic_navigation_white_48dp);
                } else {
                    navigate = true;
                    direction(widthMap, lengthMap);
                    canvasMapView.invalidate();
                    floatingDirectionButton.setImageResource(R.drawable.ic_pause_circle_filled_white_48dp);
                }
            }
        });
        floatingSwitchFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("navigate", navigate);
                bundle.putFloat("PosX", posX);
                bundle.putFloat("PosY", posY);
                bundle.putFloat("WidthMap", widthMap);
                bundle.putFloat("LengthMap", lengthMap);
                bundle.putInt("Floor", currentFloor);
                bundle.putInt("Stairs", stairsGo);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                MapFragment map = new MapFragment();
                map.setArguments(bundle);
                transaction.replace(R.id.content_main, map);
                stopTask = true;
                transaction.commit();
            }
        });
        if (navigate){
            if (markers[0] == null){
                markers = Utils.createListMarker(width, height);
            }
            direction(widthMap, lengthMap);
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) getActivity()).setActionBarTitle(getString(R.string.ACTION_BAR_TITLE_MAPS));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = getView().findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.Refresh1, R.color.Refresh2, R.color.Refresh3, R.color.Refresh4);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onRefresh() {
                final FragmentManager mng = getActivity().getSupportFragmentManager();
                swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        canvasMapView.invalidate();
                    }
                }, 1000);
            }
        });
    }


    //create View with Canvas for map
    private class CanvasMapView extends View {
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

            //get location from GPSRouter class

            String filename = "floor1";
            Bitmap map;
            Bitmap scaleMap;
            //get location from GPSRouter class
            for (int i = 0; i < listMap.size(); i++) {
                try {
                    JSONObject object = new JSONObject(listMap.get(i));
                    if (currentFloor == object.getInt("Floor")) {
                        mapId = object.getInt("Id");
                        String sharePreferenceString = sharedPreference.getString("LASTFLOOR", "");
                        if (sharePreferenceString != "" && currentFloor == Integer.parseInt(sharedPreference.getString("LASTFLOOR", ""))) {
//                            String roomJson = sharedPreference.getString("ROOMLIST", null);
                            String cornerJson = sharedPreference.getString("CORNERLIST", null);
                            convertToCornerArray(cornerJson);
//                            convertToRoomArray(roomJson);
                        } else {
                            new MapSearchRoomFragment.initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mapId);
                            editor.putString("LASTFLOOR", currentFloor + "").apply();
                        }
                        filename = object.getString("Name");
                        break;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onDraw: JSONException", e);
                    e.printStackTrace();
                }
            }
            if (first) {
                new MapSearchRoomFragment.initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mapId);
                markers = Utils.createListMarker(width, height);
            }
            String path = sharedData.storage + filename + ".png";
            final File temp = new File(sharedData.storage + filename + ".png");

            //log 4 test
            Log.e(TAG, "onDraw: DECODE FILE PATH" + path);
            map = BitmapFactory.decodeFile(path);
            scaleMap = Bitmap.createScaledBitmap(map, width, height, false);
            canvas.drawBitmap(scaleMap, 0, 0, null);
            mPaint.setStrokeWidth(20);
            if (first == false) {
                if (posX != 0 || posY != 0) {
                    mPaint.setColor(Color.BLUE);
                    canvas.drawCircle(posX, posY, 10, mPaint);

                    Bitmap iconPlace = BitmapFactory.decodeResource(getResources(), R.drawable.ic_place_blue);
                    int widthScale = iconPlace.getWidth() / 3 * 2;
                    int heightScale = iconPlace.getHeight() / 3 * 2;
                    iconPlace = Bitmap.createScaledBitmap(iconPlace, widthScale, heightScale, false);
                    canvas.drawBitmap(iconPlace, posX - (widthScale / 2), posY - heightScale, mPaint);
                }
            }
            if (directionPoints != null) {
                int sizeDirection = directionPoints.size();
                if (directionPoints.size() > 1) {
                    mPaint.setStrokeWidth(10);
                    mPaint.setColor(Color.GREEN);

                    for (int i = 0; i < sizeDirection - 1; i++) {
                        DirectionPoint point1 = directionPoints.get(i);
                        DirectionPoint point2 = directionPoints.get(i + 1);
                        canvas.drawLine(point1.getPosX(), point1.getPosY(), point2.getPosX(), point2.getPosY(), mPaint);
                    }
                }
            }
//            if (corners != null) {
//                if (corners.length == 4) {
//                    first = false;
//                }
//            }

        }

        private void initPaint() {
            mPaint.setColor(Color.BLUE);
            mPaint.setStrokeWidth(20);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND); // Cho dau cac duong ve duoc bo tron
            mPaint.setAlpha(150);
        }
    }

    public static void direction(float posXT, float posYT) {
        directionPoints.clear();
        directionPoints.add(new DirectionPoint(posX, posY));
        int corner = 1;
        if (posXT >= 12) {
            corner = 3;
        }
        if (stairsGo == 0) {
            if (corner == 1) {
                Marker marker = markers[0];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                marker = markers[4];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
            } else {
                Marker marker = markers[3];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                marker = markers[4];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
            }
        } else {
            if (corner == 1) {
                Marker marker = markers[1];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                marker = markers[5];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
            } else {
                Marker marker = markers[2];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                marker = markers[5];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
            }
        }
    }

    public static void getPointMap(double latitude, double longitude, boolean isDevice) {
        int corner = 1;
        double min = Utils.PerpendicularDistance(corners[0], corners[1], longitude, latitude);
        double perpendicular = Utils.PerpendicularDistance(corners[1], corners[2], longitude, latitude);
        if (perpendicular <= min) {
            min = perpendicular;
            corner = 2;
        }
        perpendicular = Utils.PerpendicularDistance(corners[2], corners[3], longitude, latitude);
        if (perpendicular <= min) {
            min = perpendicular;
            corner = 3;
        }
        perpendicular = Utils.PerpendicularDistance(corners[3], corners[0], longitude, latitude);
        if (perpendicular <= min) {
            min = perpendicular;
            corner = 4;
        }
        Log.e("perpendicular: ", min + "");
        float checkX = 0;
        float checkY = 0;
        if (isDevice) {
            if (corner == 1) {
                //29  18
                Corner currentCorner1 = corners[1];
                Corner currentCorner2 = corners[0];
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                devicePosY = (float) (height / distanceCorner * temp);
                currentCorner2 = corners[2];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                double x = min + 3;
                devicePosX = (float) (width / distanceCorner * x);
            } else if (corner == 3) {
                Corner currentCorner1 = corners[2];
                Corner currentCorner2 = corners[3];
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                devicePosY = (float) (height / distanceCorner * temp);
                currentCorner2 = corners[1];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                double x = distanceCorner - (min + 3);
                devicePosX = (float) (width / distanceCorner * x);
            } else if (corner == 2) {
                Corner currentCorner1 = corners[2];
                Corner currentCorner2 = corners[1];
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2) + 3;
                devicePosY = (float) (height / distanceCorner * temp);
                currentCorner2 = corners[3];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                devicePosX = (float) (width / distanceCorner * min);
            } else if (corner == 4) {
                Corner currentCorner1 = corners[3];
                Corner currentCorner2 = corners[0];
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                devicePosY = (float) (height / distanceCorner * temp) + 3;
                currentCorner2 = corners[2];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                devicePosX = (float) (width / distanceCorner * min);
            }
        } else {
            Log.d("Corner:", corner + "");
            if (corner == 1) {
                //29  18
                Corner currentCorner1 = corners[1];
                Corner currentCorner2 = corners[0];
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                checkY = (float) (height / distanceCorner * temp);
                currentCorner2 = corners[2];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                checkX = (float) (width / distanceCorner * min);
            } else if (corner == 3) {
                Corner currentCorner1 = corners[2];
                Corner currentCorner2 = corners[3];
                double distance2 = (float) Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                checkY = (float) (height / distanceCorner * temp);
                currentCorner2 = corners[1];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                double x = distanceCorner - (min);
                checkX = (float) (width / distanceCorner * x);
            } else if (corner == 2) {
                Corner currentCorner1 = corners[2];
                Corner currentCorner2 = corners[1];
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner2.getLatitude(), currentCorner2.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                checkX = (float) (width / distanceCorner * temp);
                currentCorner1 = corners[0];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                checkY = (float) (height / distanceCorner * min);
            } else if (corner == 4) {
                Corner currentCorner1 = corners[3];
                Corner currentCorner2 = corners[0];
                double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner2.getLatitude(), currentCorner2.getLongitude());
                double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                        currentCorner1.getLatitude(), currentCorner1.getLongitude());
                double temp = Utils.getPixelWithPer(min, distance2);
                checkX = (float) (width / distanceCorner * temp);
                currentCorner2 = corners[2];
                distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                        currentCorner2.getLatitude(), currentCorner2.getLongitude());
                double x = distanceCorner - min;
                checkY = (float) (height / distanceCorner * x);
            }

            if ((checkX <= width && checkX >= 0) && (checkY <= height && checkY >= 0)) {
                posX = checkX;
                posY = checkY;
            }
        }
    }


    public class initListCorner extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                int mapId = Integer.parseInt(objects[0].toString());
                URL url = new URL("http://" + sharedData.IP + "/api/Corner/GetAllCornerWithMap?&mapId=" + mapId);
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
                    editor.putString("CORNERLIST", json).apply();
                    convertToCornerArray(json);
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

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            canvasMapView.first = false;
            canvasMapView.invalidate();
        }
    }

    public class CanvasAsyncTask extends AsyncTask<Void, Double, Void> {

        private MapSearchRoomFragment fragment;

        public CanvasAsyncTask(MapSearchRoomFragment fragment) {
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

    public class GetListMap extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            buildingId = 1;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://" + sharedData.IP + "/api/Map/GetListMap?buildingId=" + buildingId);
                Log.e(TAG, "doInBackground: GetListMap" + sharedData.IP);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                final StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                //logging
                Log.e(TAG, "doInBackground: getListMap" + responseOutput.toString());
                br.close();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    convertToArray(responseOutput.toString());
                    File dir = new File(sharedData.storage);
                    if (!dir.isDirectory()) {
                        dir.mkdir();
                    }
                    for (int i = 0; i < listMap.size(); i++) {
                        String urlMap = "http://" + sharedData.IP + "/" + new JSONObject(listMap.get(i)).getString("MapUrl");
                        String nameMap = new JSONObject(listMap.get(i)).getString("Name");

                        //log 4 test
                        Log.e(TAG, "doInBackground: PATH CHECK" + sharedData.storage + nameMap);

                        File checkFile = new File(sharedData.storage + nameMap + ".png");
                        if (!checkFile.exists()) {
                            DownloadMap(urlMap, nameMap);
                        }
                    }
                }
                if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    Toast.makeText(getContext(), "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();

                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground: GetListMap", e);
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: GetListMap", e);
            } catch (JSONException e) {
                Log.e(TAG, "doInBackground: GetListMap", e);
            }
            result = "Finish";
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            FrameLayout layout = v.findViewById(R.id.canvasView);
            canvasMapView = new MapSearchRoomFragment.CanvasMapView(getContext());
            canvasMapView.setId(R.id.viewCanvas);
            layout.addView(canvasMapView);
            stopTask = false;
        }
    }

    //convert json object to array
    public void convertToArray(String s) throws JSONException {
        JSONArray array = new JSONArray(s);
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                listMap.add(array.get(i).toString());
            }
        }
    }

    public void DownloadMap(String urlMap, String nameMap) {
        Uri uri = Uri.parse(urlMap);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir("/LOAB", nameMap + ".png");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        Long reference = downloadManager.enqueue(request);
    }

    public void convertToRoomArray(String json) {
        try {
            JSONArray list = new JSONArray(json);
            int total = list.length();
            rooms = new Room[total];
            for (int i = 0; i < total; i++) {
                JSONObject jsonObject = new JSONObject(list.get(i).toString());
                Room newRoom = new Room(jsonObject.getInt("Id"), jsonObject.getString("Name"),
                        jsonObject.getInt("Floor"), jsonObject.getDouble("Length"),
                        jsonObject.getDouble("Width"), jsonObject.getInt("MapId"),
                        jsonObject.getDouble("Longitude"), jsonObject.getDouble("Latitude"));
                rooms[i] = newRoom;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void convertToCornerArray(String json) {
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
            Log.e(TAG, "doInBackground: CORNER", e);
            e.printStackTrace();
        }
    }
}
