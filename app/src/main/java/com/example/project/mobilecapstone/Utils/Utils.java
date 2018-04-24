package com.example.project.mobilecapstone.Utils;

import com.example.project.mobilecapstone.Data.Corner;
import com.example.project.mobilecapstone.Data.Marker;

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

    public static Marker[] createListMarker(float width, float height) {
        Marker[] markers = new Marker[6];

        Marker marker = new Marker();
        marker.setName("Corner");
        marker.setPosition(1);
        float temp = getPixel(width, 24F, 6);
        marker.setPosX(temp);
        temp = getPixel(height, 42F, 40);
        marker.setPosY(temp);
        markers[0] = marker;

        Marker marker1 = new Marker();
        marker1.setName("Corner");
        marker1.setPosition(2);
        temp = getPixel(width, 24F, 6);
        marker1.setPosX(temp);
        temp = getPixel(height, 42F, 2);
        marker1.setPosY(temp);
        markers[1] = marker1;

        Marker marker2 = new Marker();
        marker2.setName("Corner");
        marker2.setPosition(3);
        temp = getPixel(width, 24F, 18);
        marker2.setPosX(temp);
        temp = getPixel(height, 42F, 2);
        marker2.setPosY(temp);
        markers[2] = marker2;

        Marker marker3 = new Marker();
        marker3.setName("Corner");
        marker3.setPosition(4);
        temp = getPixel(width, 24F, 18);
        marker3.setPosX(temp);
        temp = getPixel(height, 42F, 40);
        marker3.setPosY(temp);
        markers[3] = marker3;

        Marker marker4 = new Marker();
        marker4.setName("Stairs1");
        marker4.setPosition(5);
        marker4.setPosX(0);
        temp = getPixel(height, 42F, 40);
        marker4.setPosY(temp);
        markers[4] = marker4;

        Marker marker5 = new Marker();
        marker5.setName("Stairs2");
        marker5.setPosition(6);
        marker5.setPosX(0);
        temp = getPixel(height, 42F, 2);
        marker5.setPosY(temp);
        markers[5] = marker5;

        return markers;
    }
}
