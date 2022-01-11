package com.uoa.server;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class GenericDevice {

    @Id
    private Integer device_id;
    private Double lat;
    private Double lng;
    private long lastUpdate;

    public GenericDevice(){}

    public GenericDevice(Integer device_id){
        this.device_id = device_id;
    }

    public GenericDevice(Integer device_id, Double lat, Double lng, long lastUpdate) {
        this.device_id = device_id;
        this.lat = lat;
        this.lng = lng;
        this.lastUpdate = lastUpdate;
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

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}