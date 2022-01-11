package com.uoa.server;

public class AndroidDevice extends GenericDevice {

    public AndroidDevice() {
        super();
    }

    public AndroidDevice(Integer device_id, Double lat, Double lng) {
        super(device_id, lat, lng);
    }

    public AndroidDevice(Integer device_id) {
        super(device_id);
    }

    public void update(AndroidDevice androidDevice) {}

    @Override
    public String toString() {
        return "AndroidDevice{" +
                "device_id=" + this.getDevice_id() +
                ", lat=" + this.getLat() +
                ", lng=" + this.getLng() +
                '}';
    }

}