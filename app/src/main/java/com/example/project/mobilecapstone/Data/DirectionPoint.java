package com.example.project.mobilecapstone.Data;

/**
 * Created by HieuTNSE61591 on 4/20/2018.
 */

public class DirectionPoint {
    private double posX;
    private double posY;

    public DirectionPoint(double posX, double posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public DirectionPoint() {
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }
}
