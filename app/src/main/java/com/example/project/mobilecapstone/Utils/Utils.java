package com.example.project.mobilecapstone.Utils;

/**
 * Created by ADMIN on 3/6/2018.
 */

public class Utils {
    private static double eQuatorialEarthRadius = 6371D;
    private static double d2r = (Math.PI / 180D);

    public static float getPixel(int x, int a, int b) {
        if (b == 0) {
            return x * a;
        }
        return (x * a) + (x / b);
    }

    public static double HaversineInM(double lat1, double long1, double lat2, double long2)
    {
        return (1000D * HaversineInKMFast(lat1, long1, lat2, long2));
    }

    public static double HaversineInKM(double lat1, double long1, double lat2, double long2)
    {
        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * d2r) * Math.cos(lat2 * d2r) * Math.pow(Math.sin(dlong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = eQuatorialEarthRadius * c;

        return d;
    }

    public static double HaversineInKMFast(double lat1, double long1, double lat2, double long2)
    {
        double dLat = (lat2 - lat1) * Math.PI / 180; // deg2rad below
        double dLon = (long2 - long1) * Math.PI / 180;
        double a =
                0.5 - Math.cos(dLat) / 2 +
                        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                                (1 - Math.cos(dLon)) / 2;

        return eQuatorialEarthRadius * 2 * Math.asin(Math.sqrt(a));
    }
}
