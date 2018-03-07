package com.example.project.mobilecapstone.Data;

/**
 * Created by ADMIN on 3/6/2018.
 */

public class CalculatorModel {
    private RoomModel room;
    private double cal;

    public CalculatorModel() {
    }

    public RoomModel getRoom() {
        return room;
    }

    public void setRoom(RoomModel room) {
        this.room = room;
    }

    public double getCal() {
        return cal;
    }

    public void setCal(double cal) {
        this.cal = cal;
    }

    public CalculatorModel(RoomModel room, double cal) {

        this.room = room;
        this.cal = cal;
    }
}
