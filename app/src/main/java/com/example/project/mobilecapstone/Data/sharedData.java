package com.example.project.mobilecapstone.Data;

import android.os.Environment;

/**
 * Created by admin on 3/5/2018.
 */

public class sharedData {
    public static String IP = "10.82.135.126";
    public static String storage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LOAB/";
    //tracking location
    public static Double LAT = 10.0;
    public static Double LONG = 0.0;
    public static Double ALT = 0.0;
    //search position
    public static int PosAX = 0;
    public static int PosAY = 0;
    public static int PosBX = 0;
    public static int PosBY = 0;

}
