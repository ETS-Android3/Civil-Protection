package com.uoa.server;

import java.util.List;

public class DevicesJSONResponse {

    private List<GenericDevice> androidDevicesList;
    private List<GenericDevice> iotDevicesList;
    private String status;

    public DevicesJSONResponse() {}

    public DevicesJSONResponse(List<GenericDevice> androidDevicesList, List<GenericDevice> iotDevicesList, String status) {
        this.androidDevicesList = androidDevicesList;
        this.iotDevicesList = iotDevicesList;
        this.status = status;
    }

    public void setAndroidDevicesList(List<GenericDevice> androidDevicesList) {
        this.androidDevicesList = androidDevicesList;
    }

    public void setIotDevicesList(List<GenericDevice> iotDevicesList) {
        this.iotDevicesList = iotDevicesList;
    }

    public List<GenericDevice> getAndroidDevicesList() {
        return androidDevicesList;
    }

    public List<GenericDevice> getIotDevicesList() {
        return iotDevicesList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}