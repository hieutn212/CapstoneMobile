package com.example.project.mobilecapstone.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.project.mobilecapstone.Utils.GPSRouter;
import com.example.project.mobilecapstone.MapSearchActivity;
import com.example.project.mobilecapstone.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    GPSRouter gps;
    double latitude;
    double longitude;
    double altitude;
    Bundle bundle;
    Double dLatitude, dLongitude;
    private static final String TAG = "MapFragment";

    public MapFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        //get location from GPSRouter class
        gps = new GPSRouter(getContext());
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            altitude = gps.getAltitude();
        } else {
            gps.showSettingAlert();
        }
        //create btn_searchMap onclick handler
        Button searchButton = (Button) v.findViewById(R.id.btn_searchMap);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getBaseContext(), MapSearchActivity.class);
                getActivity().getBaseContext().startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map1);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        } else {
            //check for incoming device position from tracking fragment
            Log.e(TAG, "onMapReady: device location" + dLatitude + " " + dLongitude);
            checkTrackingPosition();
            if (dLatitude == null || dLongitude == null) {
                LatLng hcm = new LatLng(latitude, longitude);
                map.moveCamera(CameraUpdateFactory.newLatLng(hcm));
                map.moveCamera(CameraUpdateFactory.zoomTo(20f));
            } else {
                LatLng pinPoint = new LatLng(dLatitude, dLongitude);
                map.addMarker(new MarkerOptions().position(new LatLng(dLatitude, dLongitude)).title("your device is here !! "));
                map.moveCamera(CameraUpdateFactory.newLatLng(pinPoint));
                map.moveCamera(CameraUpdateFactory.zoomTo(20f));
            }
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
    }

    public void checkTrackingPosition() {
        bundle = getArguments();
        if (bundle != null && bundle.containsKey("LAT") && bundle.containsKey("LONG")) {
            dLatitude = Double.parseDouble(bundle.getString("LAT"));
            dLongitude = Double.parseDouble(bundle.getString("LONG"));
        }
    }
}
