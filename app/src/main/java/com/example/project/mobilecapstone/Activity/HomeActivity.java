package com.example.project.mobilecapstone.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import com.example.project.mobilecapstone.Data.sharedData;
import com.example.project.mobilecapstone.Fragment.MapFragment;
import com.example.project.mobilecapstone.Fragment.MapTrackingFragment;
import com.example.project.mobilecapstone.Fragment.TrackingFragment;
import com.example.project.mobilecapstone.R;

import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_CODE_ROOM = 0x9345;
    FragmentManager fm = getSupportFragmentManager();
    UserInfo userInfo = new UserInfo();
    private static final String TAG = "HomeActivity";
    List<Fragment> fragmentList;
    FloatingActionButton floatingDirectionButton;
    FloatingActionButton floatingSwitchFloorButton;

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
        //floating buttons
        floatingDirectionButton = findViewById(R.id.navigation);
        floatingSwitchFloorButton = findViewById(R.id.switch_floor);
        // Set username & fullname
        txtUsername.setText(userInfo.getUserName());
        txtFullname.setText(userInfo.getFullName());
        fm.beginTransaction().replace(R.id.content_main, new MapFragment(), "MAP").commit();
        //set home fragment selected on launch
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
        }else if (floatingDirectionButton.getVisibility() == View.VISIBLE || floatingSwitchFloorButton.getVisibility() == View.VISIBLE){
            floatingDirectionButton.setVisibility(View.INVISIBLE);
            floatingSwitchFloorButton.setVisibility(View.INVISIBLE);
            fm.beginTransaction().replace(R.id.content_main, new MapFragment(), "MAP").commit();
        } else {
            if (fm.getBackStackEntryCount() > 0){
                fm.popBackStack();
            }else{
                new AlertDialog.Builder(this).setTitle("Thoát ứng dụng").setMessage("Bạn có muốn thoát ứng dụng ?").setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fragmentList = fm.getFragments();
                        Fragment temp = fragmentList.get(fragmentList.size() - 1);
                        if (temp.getClass().toString() == MapFragment.class.toString()) {
                            MapFragment fragment = (MapFragment) temp;
                            fragment.stopTask = true;
                        }
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
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        int id = item.getItemId();

        /*if (id == R.id.nav_search) {
            Intent intent = new Intent(HomeActivity.this, MapSearchActivity.class);
            startActivityForResult(intent,REQUEST_CODE_ROOM);
        }*/
        if (id == R.id.nav_map) {
            fragmentList = fm.getFragments();
            Fragment temp = fragmentList.get(fragmentList.size() - 1);
            if (temp.getClass().getName().equals(MapTrackingFragment.class.getName())) {
                MapTrackingFragment fragment = (MapTrackingFragment) temp;
                fragment.stopTask = true;
            }
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.content_main, new MapFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_trackers) {
            if (userInfo.getPurchasePack().equals("2")){
                fragmentList = fm.getFragments();
                Fragment temp = fragmentList.get(fragmentList.size() - 1);
                if (temp.getClass().getName().equals(MapFragment.class.getName())) {
                    MapFragment fragment = (MapFragment) temp;
                    fragment.stopTask = true;
                }
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.content_main, new TrackingFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
            else{
                new AlertDialog.Builder(HomeActivity.this).setTitle("Tài khoản hết hạn").setMessage("Tài khoản bạn không dùng được chức năng này,\nBạn có muốn đến trang web để mua tài khoản VIP?").setPositiveButton("Đến web", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + sharedData.IP + ":36110/"));
                        startActivity(browserIntent);
                    }
                }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
            }
        } /*else if (id == R.id.nav_settings) {

        }*/ else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(this).setTitle("Đăng xuất ?").setMessage("Bạn có muốn đăng xuất ?").setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    fragmentList = fm.getFragments();
                    Fragment temp = fragmentList.get(fragmentList.size() - 1);
                    if (temp.getClass().toString() == MapFragment.class.toString()) {
                        MapFragment fragment = (MapFragment) temp;
                        fragment.stopTask = true;
                    }
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
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
        userInfo.setPurchasePack(getIntent().getStringExtra("packType"));
        Log.e(TAG, "getUserInfo: " + userInfo.getId() + userInfo.getFullName() + userInfo.getUserName());
    }

    //set Actionbar title
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
