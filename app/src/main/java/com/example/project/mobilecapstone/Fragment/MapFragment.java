package com.example.project.mobilecapstone.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.project.mobilecapstone.Activity.HomeActivity;
import com.example.project.mobilecapstone.Data.Corner;
import com.example.project.mobilecapstone.Data.DirectionPoint;
import com.example.project.mobilecapstone.Data.Marker;
import com.example.project.mobilecapstone.Data.Room;
import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.MapSearchActivity;
import com.example.project.mobilecapstone.R;
import com.example.project.mobilecapstone.Utils.GPSRouter;
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


public class MapFragment extends Fragment implements View.OnClickListener {

    public boolean stopTask = true;
    GPSRouter gps;
    static double latitude;
    static double longitude;
    static double altitude;
    int mapId = 0;
    static float posX = 0;
    static float posY = 0;
    private static int currentFloor = 0;
    public static Room[] rooms = null;
    public static Corner[] corners = new Corner[]{};
    static float roomPosX = 0;
    static float roomPosY = 0;
    static float roomPosXSwitch = 0;
    static float roomPosYSwitch = 0;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ArrayList<String> listMap = new ArrayList<String>();
    Integer buildingId;
    private DownloadManager downloadManager;
    public SwipeRefreshLayout swipeRefreshLayout;
    static int width = 0;
    static int height = 0;
    private static final String TAG = "MapFragment";
    CanvasMapView canvasMapView;
    boolean reDraw = false;
    View v;
    boolean downloadCompleted = true;
    Button btnSearch;
    private static final int REQUEST_CODE_ROOM = 0x9345;
    Fragment fragment;
    private String result = "";
    boolean first = true;
    int checkFloor = 0;
    ProgressBar progressBar;
    int countChangeFloor = 0;
    int getFloor = -1;
    FloatingActionButton floatingDirectionButton;
    FloatingActionButton floatingSwitchFloorButton;
    String nameMap = "";
    boolean navigate = false;
    static Marker[] markers = new Marker[6];
    float widthMap = 0;
    float lengthMap = 0;
    static int currentCorner = 0;
    static List<DirectionPoint> directionPoints = new ArrayList<>();
    static int roomFloor = 0;
    static double latitudeUpdate = 0;
    static double longitudeUpdate = 0;
    FragmentManager fragmentManager;

    public MapFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            widthMap = bundle.getFloat("WidthMap");
            lengthMap = bundle.getFloat("LengthMap");
            roomFloor = bundle.getInt("Floor");
//            stairsGo = bundle.getInt("Stairs");
            roomPosXSwitch = bundle.getFloat("PosX");
            roomPosYSwitch = bundle.getFloat("PosY");
            navigate = bundle.getBoolean("navigate");
        }
        downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        sharedPreference = getActivity().getSharedPreferences("ROOM_CORNER_INFO", getActivity().MODE_PRIVATE);
        editor = sharedPreference.edit();
        fragmentManager = getActivity().getSupportFragmentManager();
        new GetListMap().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        new initListRoom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
        fragment = this;
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_map, container, false);
        //assign text view
        btnSearch = v.findViewById(R.id.buttonSearch);
        Activity activity = getActivity();
        progressBar = activity.findViewById(R.id.progressBar);
        btnSearch.setOnClickListener(this);
        floatingDirectionButton = activity.findViewById(R.id.navigation);
        floatingSwitchFloorButton = activity.findViewById(R.id.switch_floor);
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
        //init button click listener
        floatingSwitchFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int stairsGo = getStairsGo();
                Bundle bundle = new Bundle();
                bundle.putFloat("PosX", roomPosXSwitch);
                bundle.putFloat("PosY", roomPosYSwitch);
                bundle.putFloat("WidthMap", widthMap);
                bundle.putFloat("LengthMap", lengthMap);
                bundle.putInt("Floor", roomFloor);
                bundle.putInt("Stairs", stairsGo);
                bundle.putBoolean("navigate", navigate);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                MapSearchRoomFragment map = new MapSearchRoomFragment();
                map.setArguments(bundle);
                transaction.replace(R.id.content_main, map);
                stopTask = true;
                canvasMapView.invalidate();
                transaction.commit();
            }
        });
        if (navigate) {
            direction(widthMap, lengthMap);
            floatingDirectionButton.setImageResource(R.drawable.ic_pause_circle_filled_white_48dp);
        } else {
            directionPoints.clear();
            floatingDirectionButton.setImageResource(R.drawable.ic_navigation_white_48dp);
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) getActivity()).setActionBarTitle(getString(R.string.ACTION_BAR_TITLE_MAPS));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSearch:
                Intent i = new Intent(getActivity(), MapSearchActivity.class);
                startActivityForResult(i, REQUEST_CODE_ROOM);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (navigate) {
            navigate = false;
            directionPoints.clear();
            floatingDirectionButton.setImageResource(R.drawable.ic_navigation_white_48dp);
        }
        if (requestCode == REQUEST_CODE_ROOM) {
            if (resultCode == 1) {
                //Draw a room point
                widthMap = (float) data.getDoubleExtra("Width", 0);
                lengthMap = (float) data.getDoubleExtra("Length", 0);
                final int floor = data.getIntExtra("Floor", 0);
                roomFloor = floor;
                String message = "";
                final float tempRoomX = Utils.getPixel(width, 24F, widthMap);
                final float tempRoomY = Utils.getPixel(height, 42F, lengthMap);
                if (floor > currentFloor) {
                    message = "Bạn phải đi lên tầng " + floor + ". Bạn có muốn xem bản đồ của phòng không?";
                    confirmDialog(message, tempRoomX, tempRoomY, floor);
                } else if (floor < currentFloor) {
                    message = "Bạn phải đi xuống tầng " + floor + ". Bạn có muốn xem bản đồ của phòng không?";
                    confirmDialog(message, tempRoomX, tempRoomY, floor);
                } else {
                    roomPosX = tempRoomX;
                    roomPosY = tempRoomY;
                    floatingDirectionButton.setVisibility(View.VISIBLE);
                    floatingSwitchFloorButton.setVisibility(View.VISIBLE);
                }
            }
            if (resultCode == 0) {
                //Write your code if there's no result
            }
        }
    }

    private void confirmDialog(String message, final float posX, final float posY, final int floor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder
                .setMessage(message)
                .setPositiveButton("Xem bản đồ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int stairsGo = getStairsGo();
                        Bundle bundle = new Bundle();
                        bundle.putFloat("PosX", posX);
                        bundle.putFloat("PosY", posY);
                        bundle.putFloat("WidthMap", widthMap);
                        bundle.putFloat("LengthMap", lengthMap);
                        bundle.putInt("Floor", floor);
                        bundle.putInt("Stairs", stairsGo);
                        /*FragmentManager fragmentManager = getActivity().getSupportFragmentManager();*/
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        MapSearchRoomFragment map = new MapSearchRoomFragment();
                        map.setArguments(bundle);
                        transaction.replace(R.id.content_main, map).addToBackStack("MapSearchRoomFragment");
                        stopTask = true;
                        floatingDirectionButton.setVisibility(View.VISIBLE);
                        floatingSwitchFloorButton.setVisibility(View.VISIBLE);
                        transaction.commit();
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        roomPosX = 0;
                        roomPosY = 0;
                        roomPosXSwitch = posX;
                        roomPosYSwitch = posY;
                        roomFloor = floor;
                        floatingDirectionButton.setVisibility(View.VISIBLE);
                        floatingSwitchFloorButton.setVisibility(View.VISIBLE);
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    //create View with Canvas for map
    private class CanvasMapView extends View {
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
            Context context = this.getContext();

            gps = new GPSRouter(context);
            if (gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                altitude = gps.getAltitude();
//                altitude = 10.0;
//                latitude = 10.8529952;
//                longitude = 106.6296252;
            } else {
                gps.showSettingAlert();
            }

            String filename = "";
            Bitmap map;
            Bitmap scaleMap;
            //get location from GPSRouter class
            for (int i = 0; i < listMap.size(); i++) {
                try {
                    double altitudeMap1 = new JSONObject(listMap.get(i)).getDouble("Altitude");
                    double altitudeMap2 = 0.0;
                    nameMap = new JSONObject(listMap.get(i)).getString("Name");
                    if (altitude == 0.0) {
                        getFloor = 1;
                        checkFloor = 1;
                        break;
                    } else if (i < listMap.size() - 1) {
                        altitudeMap2 = new JSONObject(listMap.get(i + 1)).getDouble("Altitude");
                        if (altitudeMap1 <= altitude && altitude < altitudeMap2) {
                            JSONObject object = new JSONObject(listMap.get(i));
                            getFloor = object.getInt("Id");
                            currentFloor = object.getInt("Floor");
                            String sharePreferenceString = sharedPreference.getString("LASTFLOOR", "");
                            if (sharePreferenceString != "" && currentFloor == Integer.parseInt(sharePreferenceString)) {
//                                String roomJson = sharedPreference.getString("ROOMLIST", null);
                                String cornerJson = sharedPreference.getString("CORNERLIST", null);
                                if (cornerJson != null && !cornerJson.isEmpty()) {
                                    convertToCornerArray(cornerJson);
                                }else{
                                    new initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
//                                convertToRoomArray(roomJson);
                            } else {
                                editor.putString("LASTFLOOR", currentFloor + "").apply();
                            }
                            break;
                        }
                    } else {
                        if (altitudeMap1 <= altitude) {
                            JSONObject object = new JSONObject(listMap.get(i));
                            getFloor = object.getInt("Id");
                            currentFloor = object.getInt("Floor");
                            String sharePreferenceString = sharedPreference.getString("LASTFLOOR", "");
                            if (sharePreferenceString != "" && currentFloor == Integer.parseInt(sharePreferenceString)) {
//                                String roomJson = sharedPreference.getString("ROOMLIST", null);
                                String cornerJson = sharedPreference.getString("CORNERLIST", null);
                                if (cornerJson != null && !cornerJson.isEmpty()) {
                                    convertToCornerArray(cornerJson);
                                }else{
                                    new initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
//                                convertToRoomArray(roomJson);
                            } else {
                                editor.putString("LASTFLOOR", currentFloor + "").apply();
                            }
                            break;
                        }
                    }
                    int changeTimes = 4;
                    if (getFloor != checkFloor && countChangeFloor == 0 && first == false) {
                        checkFloor = getFloor;
                        countChangeFloor++;
                    } else if (getFloor != checkFloor && countChangeFloor < changeTimes) {
                        checkFloor = getFloor;
                        countChangeFloor = 0;
                        filename = nameMap;
//                        new initListRoom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
                        new initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, checkFloor);
                    } else if (getFloor == checkFloor && checkFloor != mapId && countChangeFloor < changeTimes) {
                        countChangeFloor++;
                    } else if (getFloor == checkFloor && countChangeFloor >= changeTimes) {
                        checkFloor = getFloor;
                        countChangeFloor = 0;
                        filename = nameMap;
//                        new initListRoom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
                        new initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, checkFloor);
                    }
                    mapId = checkFloor;
                } catch (JSONException e) {
                    Log.e(TAG, "onDraw: JSONException", e);
                    e.printStackTrace();
                }
            }
            if (first) {
                markers = Utils.createListMarker(width, height);
                mapId = getFloor;
//                new initListRoom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
                new initListCorner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mapId);
            }
            filename = nameMap;
            String path = sharedData.storage + filename + ".png";
            final File temp = new File(sharedData.storage + filename + ".png");

            //log 4 test
            Log.e(TAG, "onDraw: DECODE FILE PATH" + path);
            map = BitmapFactory.decodeFile(path);
            scaleMap = Bitmap.createScaledBitmap(map, width, height, false);
            canvas.drawBitmap(scaleMap, 0, 0, null);
            mPaint.setStrokeWidth(20);

            if (first == false) {
                if (reDraw) {
                    gps = new GPSRouter(context);
                    if (gps.canGetLocation()) {
                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();
                        altitude = gps.getAltitude();
//                        latitude = 10.8529983;
//                        longitude = 106.6296344;
                    } else {
                        gps.showSettingAlert();
                    }
                    latitudeUpdate = latitude;
                    longitudeUpdate = longitude;
                    getPointMap(latitudeUpdate, longitudeUpdate);
                    reDraw = false;
                }
                if (posX != 0 || posY != 0) {
                    mPaint.setColor(Color.BLUE);
                    canvas.drawCircle(posX, posY, 10, mPaint);
                }
            }
            if (roomPosX != 0 || roomPosY != 0) {
                mPaint.setColor(Color.CYAN);
                canvas.drawCircle(roomPosX, roomPosY, 10, mPaint);

                Bitmap iconPlace = BitmapFactory.decodeResource(getResources(), R.drawable.ic_place_blue);
                int widthScale = iconPlace.getWidth() / 3 * 2;
                int heightScale = iconPlace.getHeight() / 3 * 2;
                iconPlace = Bitmap.createScaledBitmap(iconPlace, widthScale, heightScale, false);

                canvas.drawBitmap(iconPlace, roomPosX - (widthScale / 2), roomPosY - heightScale, mPaint);
            }
//            canvas.drawCircle(Utils.getPixel(width, 24F, 16.8F), Utils.getPixel(height, 42F, 38.3F), 10, mPaint);
//            canvas.drawCircle(Utils.getPixel(width, 24F, 7.3F), Utils.getPixel(height, 42F, 4.3F), 10, mPaint);

            if (navigate) {
                direction(widthMap, lengthMap);
                if (currentFloor != roomFloor) {
                    roomPosX = 0;
                    roomPosY = 0;
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

            Toast.makeText(this.getContext(), "Your location is - \nLat: " +
                            latitude + "\nLong: " + longitude + "\nAltitude: " + altitude
                            + "\nX: " + posX + "\nY: " + posY + "\n" + filename,
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

    public static void getPointMap(double latitude, double longitude) {
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
        Log.d("Corner:", corner + "");
        currentCorner = corner;
        if (corner == 1) {
            //29  18
            Corner currentCorner1 = corners[1];
            Corner currentCorner2 = corners[0];
            double distance2 = Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
            double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                    currentCorner1.getLatitude(), currentCorner1.getLongitude());
            double temp = Utils.getPixelWithPer(min, distance2) + 3;
            checkY = (float) (height / distanceCorner * temp);
            currentCorner2 = corners[2];
            distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                    currentCorner2.getLatitude(), currentCorner2.getLongitude());
            checkX = (float) (width / distanceCorner * min);
            float check = Utils.getPixel(width, 24F, 7.7F);
            if (checkX > check && currentFloor != 0) {
                checkX = Utils.getPixel(width, 24F, 7.3F);
            }
        } else if (corner == 3) {
            Corner currentCorner1 = corners[2];
            Corner currentCorner2 = corners[3];
            double distance2 = (float) Utils.HaversineInM(latitude, longitude, currentCorner1.getLatitude(), currentCorner1.getLongitude());
            double distanceCorner = Utils.HaversineInM(currentCorner2.getLatitude(), currentCorner2.getLongitude(),
                    currentCorner1.getLatitude(), currentCorner1.getLongitude());
            double temp = Utils.getPixelWithPer(min, distance2);
            checkY = (float) (height / distanceCorner * temp) + 3;
            currentCorner2 = corners[1];
            distanceCorner = Utils.HaversineInM(currentCorner1.getLatitude(), currentCorner1.getLongitude(),
                    currentCorner2.getLatitude(), currentCorner2.getLongitude());
            double x = distanceCorner - (min);
            checkX = (float) (width / distanceCorner * x);
            float check = Utils.getPixel(width, 24F, 16.5F);
            if (checkX < check && currentFloor != 0) {
                checkX = Utils.getPixel(width, 24F, 16.8F);
            }
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
            float check = Utils.getPixel(height, 42F, 4.3F);
            if (checkY > check && currentFloor != 0) {
                checkY = Utils.getPixel(height, 42F, 4.3F);
            }
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
            float check = Utils.getPixel(height, 42F, 38F);
            if (checkY < check && currentFloor != 0) {
                checkY = Utils.getPixel(height, 42F, 38.3F);
            }
        }

        if ((checkX <= width && checkX >= 0) && (checkY <= height && checkY >= 0)) {
            posX = checkX;
            posY = checkY;
        }
    }

    public static int getStairsGo() {
        int stairsGo = -1;
        if (currentCorner == 1) {
            Corner cornerCloser = corners[1];
            double min = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
            cornerCloser = corners[0];
            double temp = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
            if (min > temp) {
                stairsGo = 0;
            } else {
                stairsGo = 1;
            }
        } else if (currentCorner == 2) {
            stairsGo = 1;
        } else if (currentCorner == 3) {
            Corner cornerCloser = corners[2];
            double min = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
            cornerCloser = corners[3];
            double temp = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
            if (min > temp) {
                stairsGo = 0;
            } else {
                stairsGo = 1;
            }
        } else {
            stairsGo = 0;
        }
        return stairsGo;
    }

    public static void direction(float posXT, float posYT) {
        directionPoints.clear();
        directionPoints.add(new DirectionPoint(posX, posY));
        float checkInRoomX = Utils.getPixel(width, 24F, 4);
        float checkInRoomX2 = Utils.getPixel(width, 24F, 20);
        if (checkInRoomX >= posX) {
            float temp = Utils.getPixel(width, 24F, 6);
            directionPoints.add(new DirectionPoint(temp, posY));
        } else if (posX >= checkInRoomX2) {
            float temp = Utils.getPixel(width, 24F, 18);
            directionPoints.add(new DirectionPoint(temp, posY));
        }
        int currentCorner = 1;
        if (posX >= Utils.getPixel(width, 24F, 16.5F)) {
            currentCorner = 3;
        } else if (posY <= Utils.getPixel(height, 42F, 4.3F)) {
            currentCorner = 2;
        } else if (posY >= Utils.getPixel(height, 42F, 38F)) {
            currentCorner = 4;
        }
        if (currentFloor != roomFloor) {
            if (currentCorner == 1) {
                Corner cornerCloser = corners[1];
                double min = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                cornerCloser = corners[0];
                double temp = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                if (min > temp) {
                    Marker marker = markers[0];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                    marker = markers[4];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                } else {
                    Marker marker = markers[1];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                    marker = markers[5];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                }
            } else if (currentCorner == 2) {
                Marker marker = markers[5];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
            } else if (currentCorner == 3) {
                Corner cornerCloser = corners[2];
                double min = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                cornerCloser = corners[3];
                double temp = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                if (min > temp) {
                    Marker marker = markers[3];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                    Marker markerFinal = markers[4];
                    directionPoints.add(new DirectionPoint(markerFinal.getPosX(), markerFinal.getPosY()));
                } else {
                    Marker marker = markers[2];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                    Marker markerFinal = markers[5];
                    directionPoints.add(new DirectionPoint(markerFinal.getPosX(), markerFinal.getPosY()));
                }
            } else {
                Marker marker = markers[4];
                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
            }
        } else {
            int corner = 1;
            if (posXT >= 12) {
                corner = 3;
            }

            if (currentCorner != corner) {
                if (corner == 1 && currentCorner == 2) {
                    Marker marker = markers[1];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                } else if (corner == 1 && currentCorner == 4) {
                    Marker marker = markers[0];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                } else if (corner == 3 && currentCorner == 2) {
                    Marker marker = markers[2];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                } else if (corner == 3 && currentCorner == 4) {
                    Marker marker = markers[0];
                    directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                } else {
                    if (corner == 1 && currentCorner == 3) {
                        Corner cornerCloser = corners[2];
                        double min = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                        cornerCloser = corners[3];
                        double temp = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                        if (min > temp) {
                            if (posYT <= 14) {
                                Marker marker = markers[2];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[1];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            } else {
                                Marker marker = markers[3];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[0];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            }
                        } else {
                            if (posYT > 27.5) {
                                Marker marker = markers[3];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[0];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            } else {
                                Marker marker = markers[2];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[1];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            }
                        }
                    } else if (corner == 3 && currentCorner == 1) {
                        Corner cornerCloser = corners[1];
                        double min = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                        cornerCloser = corners[0];
                        double temp = Utils.HaversineInM(latitudeUpdate, longitudeUpdate, cornerCloser.getLatitude(), cornerCloser.getLongitude());
                        if (min > temp) {
                            if (posYT <= 14) {
                                Marker marker = markers[1];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[2];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            } else {
                                Marker marker = markers[0];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[3];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            }
                        } else {
                            if (posYT < 27.5) {
                                Marker marker = markers[1];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[2];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            } else {
                                Marker marker = markers[0];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));

                                marker = markers[3];
                                directionPoints.add(new DirectionPoint(marker.getPosX(), marker.getPosY()));
                            }
                        }
                    }
                }
            }
            directionPoints.add(new DirectionPoint(roomPosX, roomPosY));
        }
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
                int buildingId = Integer.parseInt(objects[0].toString());
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
                    editor.putString("ROOMLIST", json).apply();
                    convertToRoomArray(json);
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
                URL url = new URL("http://" + sharedData.IP + "/api/Corner/GetAllCornerWithMap?mapId=" + mapId);
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
            if (corners != null) {
                if (corners.length == 4) {
                    first = false;
                }
            }
        }
    }

    public class CanvasAsyncTask extends AsyncTask<Void, Double, Void> {
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
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

    public class CanvasReDraw extends AsyncTask<Void, Double, Void> {


        public CanvasReDraw() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (stopTask == false) {
                reDraw = true;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
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
                        JSONObject object = new JSONObject(listMap.get(i));
                        String urlMap = "http://" + sharedData.IP + "/" + object.getString("MapUrl");
                        String nameMap = object.getString("Name");
                        //log 4 test
                        Log.e(TAG, "doInBackground: PATH CHECK" + sharedData.storage + nameMap);
                        File checkFile = new File(sharedData.storage + nameMap + ".png");
                        if (!checkFile.exists()) {
                            downloadCompleted = false;
                            new checkDownloadCompleted().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            DownloadMap(urlMap, nameMap);
                        }
                    }
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
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getContext(), "Đang tải map", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            if (downloadCompleted) {
                FrameLayout layout = v.findViewById(R.id.canvasView);
                canvasMapView = new CanvasMapView(getContext());
                canvasMapView.setId(R.id.viewCanvas);
                layout.addView(canvasMapView);
                stopTask = false;
                new CanvasAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new CanvasReDraw().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                swipeRefreshLayout = getView().findViewById(R.id.swipeLayout);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setColorSchemeResources(R.color.Refresh1, R.color.Refresh2, R.color.Refresh3, R.color.Refresh4);
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
                progressBar.setVisibility(View.INVISIBLE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Đang tải map!!! Vui lòng chờ ít phút!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class checkDownloadCompleted extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            buildingId = 1;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                int checkExistAll = 0;
                int size = listMap.size();
                for (int i = 0; i < size; i++) {
                    JSONObject object = new JSONObject(listMap.get(i));
                    String nameMap = object.getString("Name");
                    File checkFile = new File(sharedData.storage + nameMap + ".png");
                    if (!checkFile.exists()) {
                        downloadCompleted = false;
                        break;
                    } else if (checkFile.exists()) {
                        checkExistAll++;
                    }
                    if (checkExistAll == size) {
                        downloadCompleted = true;
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "doInBackground: GetListMap", e);
            }
            result = "Finish";
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (downloadCompleted) {
                Toast.makeText(getContext(), "Tải map hoàn tất", Toast.LENGTH_SHORT).show();
                FrameLayout layout = v.findViewById(R.id.canvasView);
                canvasMapView = new CanvasMapView(getContext());
                canvasMapView.setId(R.id.viewCanvas);
                layout.addView(canvasMapView);
                stopTask = false;
                new CanvasAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new CanvasReDraw().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                swipeRefreshLayout = getView().findViewById(R.id.swipeLayout);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setColorSchemeResources(R.color.Refresh1, R.color.Refresh2, R.color.Refresh3, R.color.Refresh4);
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
            } else {
                new checkDownloadCompleted().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
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
            Log.e(TAG, "convertToRoomArray: ", e);
        }
    }

    public void convertToCornerArray(String json) {
        try {
            JSONArray list = new JSONArray(json);
            if (list != null) {
                int total = list.length();
                Corner[] newCorners = new Corner[total];
                for (int i = 0; i < total; i++) {
                    JSONObject jsonObject = new JSONObject(list.get(i).toString());
                    Corner newCorner = new Corner(jsonObject.getInt("MapId"), jsonObject.getString("Description"),
                            jsonObject.getDouble("Longitude"), jsonObject.getDouble("Latitude"),
                            jsonObject.getInt("Id"), jsonObject.getInt("Floor"), jsonObject.getInt("Position"));
                    newCorners[i] = newCorner;
                }
                corners = newCorners;
            }
        } catch (JSONException e) {
            Log.e(TAG, "doInBackground: CORNER", e);
            e.printStackTrace();
        }
    }

    public class CanvasService extends IntentService {

        @Override
        public void onCreate() {
            super.onCreate();
        }

        public CanvasService() {
            super("CanvasService");
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            while (true) {
                canvasMapView.invalidate();
                SystemClock.sleep(3000);
            }
        }
    }
}
