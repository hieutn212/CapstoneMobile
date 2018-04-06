package com.example.project.mobilecapstone.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.mobilecapstone.Data.UserInfo;
import com.example.project.mobilecapstone.Fragment.MapFragment;
import com.example.project.mobilecapstone.Fragment.TrackingFragment;
import com.example.project.mobilecapstone.MapSearchActivity;
import com.example.project.mobilecapstone.R;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_CODE_ROOM = 0x9345;
    FragmentManager fm = getSupportFragmentManager();
    UserInfo userInfo = new UserInfo();
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //get information from loginactivity
        getUserInfo();
        //navigation View
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Navigation View Header
        View headerView = navigationView.getHeaderView(0);
        // Header elements
        TextView txtUsername = headerView.findViewById(R.id.txtUsernameNav);
        TextView txtFullname = headerView.findViewById(R.id.txtFullnameNav);
        // Set username & fullname
        txtUsername.setText(userInfo.getUserName());
        txtFullname.setText(userInfo.getFullName());
        //check for permissions
        checkPermissions();
        fm.beginTransaction().replace(R.id.content_main, new MapFragment()).commit();
        //set home fragment selected on launch

    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestLocationPermission();
        }

        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestInternetPermission();
        }

        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestReadPermission();
        }

        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestWritePermission();
        }
    }

    //Request user for internet permission
    private void requestInternetPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            new AlertDialog.Builder(this).setTitle("Permission needed")
                    .setMessage("This permission is needed for the functions to work")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }
    }
    //Request user for location permission
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new AlertDialog.Builder(this).setTitle("Permission needed")
                    .setMessage("This permission is needed for the functions to work")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
    }
    //request user to read storage
    private void requestReadPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this).setTitle("Permission needed")
                    .setMessage("This permission is needed for the functions to work")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
        }
    }
    //request user to write storage
    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this).setTitle("Permission needed")
                    .setMessage("This permission is needed for the functions to work")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
        }
    }
    //Notify user about requested permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "thank you", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "Denied permission to use internet", Toast.LENGTH_SHORT);
            }
        }
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "thank you", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "Denied permission to user location data", Toast.LENGTH_SHORT);
            }
        }
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "thank you", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "Denied permission to read from storage", Toast.LENGTH_SHORT);
            }
        }
        if (requestCode == 4) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "thank you", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "Denied permission to write to storage", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this).setTitle("Thoát ứng dụng").setMessage("Bạn có muốn thoát ứng dụng ?").setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    /*Fragment temp = fm.getFragments().get(0);
                    if (temp.getClass().toString() == MapFragment.class.toString()){
                        MapFragment fragment = (MapFragment) temp;
                        fragment.stopTask = true;
                    }*/
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            Intent intent = new Intent(HomeActivity.this, MapSearchActivity.class);
            startActivityForResult(intent,REQUEST_CODE_ROOM);
        }
        if (id == R.id.nav_map) {
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.content_main, new MapFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_trackers) {
            /*Fragment temp = fm.getFragments().get(0);
            if (temp.getClass().toString() == MapFragment.class.toString()){
                MapFragment fragment = (MapFragment) temp;
                fragment.stopTask = true;
            }*/
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.content_main, new TrackingFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(this).setTitle("Đăng xuất ?").setMessage("Bạn có muốn đăng xuất ?").setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    /*Fragment temp = fm.getFragments().get(0);
                    if (temp.getClass().toString() == MapFragment.class.toString()){
                        MapFragment fragment = (MapFragment) temp;
                        fragment.stopTask = true;
                    }*/
                    Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //Save logged in user info
    public void getUserInfo() {
        userInfo.setId(getIntent().getStringExtra("Id"));
        userInfo.setUserName(getIntent().getStringExtra("Username"));
        userInfo.setFullName(getIntent().getStringExtra("Fullname"));
        Log.e(TAG, "getUserInfo: " + userInfo.getId() + userInfo.getFullName() + userInfo.getUserName());
    }

    //set Actionbar title
    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }
}
