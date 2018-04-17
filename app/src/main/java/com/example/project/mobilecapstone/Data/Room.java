package com.example.project.mobilecapstone.Data;

/**
 * Created by ADMIN on 3/18/2018.
 */

public class Room {
    private int id;
    private String name;
    private int floor;
    private double length;
    private double width;
    private int mapId;
    private double longitude;
    private double latitude;

    public Room() {
    }

    public Room(int id, String name, int floor, double length, double width, int mapId, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.length = length;
        this.width = width;
        this.mapId = mapId;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
