package com.uoa.server;

import java.util.ArrayList;
import java.util.List;

public class DevicesList {

    public List<GenericDevice> devicesList;

    public DevicesList() {
        this.devicesList = new ArrayList<>();
    }

    public void add(GenericDevice newDevice) {
        boolean alreadyExists = false;
        String typeOfDevice = (newDevice instanceof AndroidDevice) ? "client" : "sensor";
        for (int i = 0; i < devicesList.size(); i++) {
            if (devicesList.get(i).getDevice_id().intValue() == newDevice.getDevice_id().intValue()) {
                System.out.println("Updating " + typeOfDevice + " device info...");
                alreadyExists = true;
                devicesList.set(i, newDevice);
                break;
            }
        }

        if (!alreadyExists) {
            System.out.println("Inserting new " + typeOfDevice + " device...");
            this.devicesList.add(newDevice);
        }

    }

    public Integer size() {
        return this.devicesList.size();
    }

    public GenericDevice get(int index) {
        return this.devicesList.get(index);
    }

}