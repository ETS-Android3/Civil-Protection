package com.civilprotectionsensor;

import androidx.annotation.NonNull;

public class Sensor {

    private String type;
    private double min;
    private double max;
    private double current;

    public Sensor() {}

    public Sensor(String type, double min, double max, double current) {
        this.type = type;
        this.min = min;
        this.max = max;
        this.current = current;
    }

    @NonNull
    @Override
    public String toString() {
        return "type:" + getType() + ";min:" + getMin() + ";max:" + getMax() + ";current:" + getCurrent();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

}