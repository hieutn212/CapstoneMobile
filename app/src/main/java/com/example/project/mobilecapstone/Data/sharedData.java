package com.example.project.mobilecapstone.Data;

import android.os.Environment;

/**
 * Created by admin on 3/5/2018.
 */

public class sharedData {
    //IP of server

    public static String IP = "169.254.34.32";
    public static String storage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LOAB/";
    //tracking location
    public static Double LAT = 0.0;
    public static Double LONG = 0.0;
    public static Double ALT = 0.0;
}
