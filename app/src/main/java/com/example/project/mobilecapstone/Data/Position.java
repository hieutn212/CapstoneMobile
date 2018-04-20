package com.example.project.mobilecapstone.Data;

public class Position {
    private Double Lat;
    private Double Long;
    private Double Alt;

    public Position(Double lat, Double aLong, Double alt) {
        Lat = lat;
        Long = aLong;
        Alt = alt;
    }

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLong() {
        return Long;
    }

    public void setLong(Double aLong) {
        Long = aLong;
    }

    public Double getAlt() {
        return Alt;
    }

    public void setAlt(Double alt) {
        Alt = alt;
    }
}
