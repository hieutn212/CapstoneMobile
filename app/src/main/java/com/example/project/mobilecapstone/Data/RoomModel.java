package com.example.project.mobilecapstone.Data;

/**
 * Created by ADMIN on 3/6/2018.
 */

public class RoomModel {
    private String name;
    private int id;
    private int floor;
    private double length;
    private double width;
    private int mapId;
    private double longitude;

    public RoomModel(String name, int id, int floor, double length, double width, int mapId, double longitude, double latitude, int posAX, int posAY, int posBX, int posBY) {
        this.name = name;
        this.id = id;
        this.floor = floor;
        this.length = length;
        this.width = width;
        this.mapId = mapId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.posAX = posAX;
        this.posAY = posAY;
        this.posBX = posBX;
        this.posBY = posBY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getPosAX() {
        return posAX;
    }

    public void setPosAX(int posAX) {
        this.posAX = posAX;
    }

    public int getPosAY() {
        return posAY;
    }

    public void setPosAY(int posAY) {
        this.posAY = posAY;
    }

    public int getPosBX() {
        return posBX;
    }

    public void setPosBX(int posBX) {
        this.posBX = posBX;
    }

    public int getPosBY() {
        return posBY;
    }

    public void setPosBY(int posBY) {
        this.posBY = posBY;
    }

    private double latitude;
    private int posAX;
    private int posAY;
    private int posBX;
    private int posBY;
}
