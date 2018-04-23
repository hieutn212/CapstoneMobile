package com.example.project.mobilecapstone.Data;

/**
 * Created by HieuTNSE61591 on 4/20/2018.
 */

public class Marker {
    private String name;
    private float posX;
    private float posY;
    private int position;

    public Marker() {
    }

    public Marker(String name, float posX, float posY, int position) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
