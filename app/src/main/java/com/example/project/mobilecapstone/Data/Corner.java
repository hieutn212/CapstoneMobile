package com.example.project.mobilecapstone.Data;

/**
 * Created by ADMIN on 3/16/2018.
 */

public class Corner {
    private int mapId;
    private String description;
    private double longitude;
    private double latitude;
    private int id;
    private int floor;
    private int position;

    public Corner() {
    }

    public Corner(int mapId, String description, double longitude, double latitude, int id, int floor, int position) {
        this.mapId = mapId;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;
        this.floor = floor;
        this.position = position;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
