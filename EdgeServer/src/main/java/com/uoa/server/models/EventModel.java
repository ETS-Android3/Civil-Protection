package com.uoa.server.models;

import javax.persistence.*;

@Entity
@Table(name = "event")
public class EventModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Integer devId;
    private Double lat;
    private Double lng;
    private String timestamp;
    private String severity_level;
    private Double smoke;
    private Double gas;
    private Double temperature;
    private Double uv;
    private String message;

    public EventModel() {}

    public EventModel(String timestamp, Integer devId, Double lat, Double lng, String severity_level, Double smoke, Double gas, Double temperature, Double uv, String message) {
        this.timestamp = timestamp;
        this.devId = devId;
        this.lat = lat;
        this.lng = lng;
        this.severity_level = severity_level;
        this.smoke = smoke;
        this.gas = gas;
        this.temperature = temperature;
        this.uv = uv;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public Integer getDevId() {
        return devId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSeverity_level() {
        return severity_level;
    }

    public Double getSmoke() {
        return smoke;
    }

    public Double getGas() {
        return gas;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getUv() {
        return uv;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getMessage() {
        return message;
    }

}