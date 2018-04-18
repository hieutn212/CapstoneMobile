package com.example.project.mobilecapstone.Utils;

import com.example.project.mobilecapstone.Data.Corner;

/**
 * Created by ADMIN on 3/6/2018.
 */

public class Utils {
    private static double eQuatorialEarthRadius = 6371D;
    private static double d2r = (Math.PI / 180D);

//    public static float getPixel(int x, int a, int b) {
//        if (b == 0) {
//            return x * a;
//        }
//        return (x * a) + (x / b);
//    }

    public static float getPixel(float x, float y, float a) {
        return (x * a) / y;
    }

    public static double HaversineInM(double lat1, double long1, double lat2, double long2) {
        return (1000D * HaversineInKMFast(lat1, long1, lat2, long2));
    }

    public static double HaversineInKM(double lat1, double long1, double lat2, double long2) {
        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * d2r) * Math.cos(lat2 * d2r) * Math.pow(Math.sin(dlong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = eQuatorialEarthRadius * c;

        return d;
    }

    public static double HaversineInKMFast(double lat1, double long1, double lat2, double long2) {
        double dLat = (lat2 - lat1) * Math.PI / 180; // deg2rad below
        double dLon = (long2 - long1) * Math.PI / 180;
        double a =
                0.5 - Math.cos(dLat) / 2 +
                        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                                (1 - Math.cos(dLon)) / 2;

        return eQuatorialEarthRadius * 2 * Math.asin(Math.sqrt(a));
    }

    public static double PerpendicularDistance(Corner pointA, Corner pointB, double longitude, double latitude) {
        // Area = |(1/2)(x1y2 + x2y3 + x3y1 - x2y1 - x3y2 - x1y3)|   *Area of triangle
        // Base = √((x1-x2)²+(x1-x2)²)                               *Base of Triangle*
        // Area = .5*Base*H                                          *Solve for height
        // Height = Area/.5/Base

//        double area = Math.abs(.5 * (pointA.getLatitude() * pointB.getLongitude() + pointB.getLatitude() * longitude +
//                latitude * pointA.getLongitude() - pointB.getLatitude() * pointA.getLongitude() - latitude * pointB.getLongitude()
//                - pointA.getLatitude() * longitude));
//        double bottom = Math.sqrt(Math.pow(pointA.getLatitude() - pointB.getLatitude(), 2) + Math.pow(pointA.getLongitude() - pointB.getLongitude(), 2));
//        double height = area / bottom * 2;
        double bottom = HaversineInKMFast(pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(), pointB.getLongitude());
        double a = HaversineInKMFast(pointA.getLatitude(), pointA.getLongitude(), latitude, longitude);
        double b = HaversineInKMFast(latitude, longitude, pointB.getLatitude(), pointB.getLongitude());
        double p = .5 * (bottom + a + b);
        double area = Math.sqrt(p * (p - bottom) * (p - a) * (p - b));
//        double bottom = Math.sqrt(Math.pow(pointA.getLatitude() - pointB.getLatitude(), 2) + Math.pow(pointA.getLongitude() - pointB.getLongitude(), 2));
        double height = area / bottom * 2;
        return height * 1000D;
    }

    public static double getPixelWithPer(double perpendicular, double currentDistance) {
        double temp = Math.pow(currentDistance, 2);
        temp = temp - Math.pow(perpendicular, 2);
        temp = Math.abs(temp);
        double result = Math.sqrt(temp);

        return result;
    }
}
