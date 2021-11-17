package com.civilprotectionsensor;

import androidx.annotation.NonNull;

public class Sensor {

    private String type;
    private Float min;
    private Float max;
    private Float current;

    public Sensor() {}

    public Sensor(String type, Float min, Float max, Float current) {
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

    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    public Float getCurrent() {
        return current;
    }

    public void setCurrent(Float current) {
        this.current = current;
    }

}