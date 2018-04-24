package com.example.project.mobilecapstone.Data;

/**
 * Created by HieuTNSE61591 on 4/20/2018.
 */

public class DirectionPoint {
    private float posX;
    private float posY;

    public DirectionPoint(float posX, float posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public DirectionPoint() {
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
}
