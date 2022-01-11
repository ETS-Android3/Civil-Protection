package com.uoa.server;

import javax.persistence.Id;

public class GenericDevice {

    @Id
    private Integer device_id;
    private Double lat;
    private Double lng;

    public GenericDevice(){}

    public GenericDevice(Integer device_id){
        this.device_id = device_id;
    }

    public GenericDevice(Integer device_id, Double lat, Double lng) {
        this.device_id = device_id;
        this.lat = lat;
        this.lng = lng;
    }

    public Integer getDevice_id() {
        return device_id;
    }

    public void setDevice_id(Integer device_id) {
        this.device_id = device_id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

}