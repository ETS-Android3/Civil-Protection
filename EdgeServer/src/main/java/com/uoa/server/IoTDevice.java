package com.uoa.server;

import org.springframework.stereotype.Component;

@Component
public class IoTDevice extends GenericDevice {

    private Double battery_level;
    private Double smoke = null;
    private static final double smokeLimit = 0.14;
    private Double gas = null;
    private static final double gasLimit = 9.15;
    private Double temp = null;
    private static final double tempLimit = 50;
    private Double uv = null;
    private static final double uvLimit = 6;
    public static final String DANGER_LEVEL_HIGH = "DANGER_LEVEL_HIGH";
    public static final String DANGER_LEVEL_MEDIUM = "DANGER_LEVEL_MEDIUM";
    // Danger scenarios: 1, 2, 3, 4
    public static final String[] dangerMsg = {
        "High smoke and gas levels detected in your area!",
        "High temperature and UV radiation in your area!",
        "High gas levels detected in your area!",
        "This is probably the last day on earth!"
    };

    public IoTDevice() {
        super();
    }

    public IoTDevice(Integer device_id) {
        super(device_id);
    }

    public IoTDevice(Integer device_id, Double lat, Double lng, Double battery_level) {
        super(device_id, lat, lng);
        this.battery_level = battery_level;
    }

    public Double getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(Double battery_level) {
        this.battery_level = battery_level;
    }

    public Double getSmoke() {
        return smoke;
    }

    public void setSmoke(Double smoke) {
        this.smoke = smoke;
    }

    public boolean smokeInDanger() {
        return this.getSmoke() != null && this.getSmoke() > smokeLimit;
    }

    public Double getGas() {
        return gas;
    }

    public void setGas(Double gas) {
        this.gas = gas;
    }

    public boolean gasInDanger() {
        return this.getGas() != null && this.getGas() > gasLimit;
    }

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    public boolean tempInDanger() {
        return this.getTemp() != null && this.getTemp() > tempLimit;
    }

    public Double getUv() {
        return uv;
    }

    public void setUv(Double uv) {
        this.uv = uv;
    }

    public boolean uvInDanger() {
        return this.getUv() != null && this.getUv() > uvLimit;
    }

    public String getDangerLevel() {
        int dangerCode = eventDetection();
        String dangerLevel = "";
        if (dangerCode == 2) dangerLevel = IoTDevice.DANGER_LEVEL_MEDIUM;
        else if (dangerCode != 0) dangerLevel = IoTDevice.DANGER_LEVEL_HIGH;
        return dangerLevel;
    }

    // Returns 0 if no danger is detected or 1-4 for each danger scenario
    public int eventDetection() {
        if (smokeInDanger() && gasInDanger() && tempInDanger() && uvInDanger()) return 4;
        else if (!smokeInDanger() && gasInDanger() && !tempInDanger() && !uvInDanger()) return 3;
        else if (!smokeInDanger() && !gasInDanger() && tempInDanger() && uvInDanger()) return 2;
        else if (smokeInDanger() && gasInDanger()) return 1;
        else return 0;
    }

    @Override
    public String toString() {
        return "IoTDevice{" +
                "device_id=" + this.getDevice_id() +
                ", lat=" + this.getLat() +
                ", lng=" + this.getLng() +
                ", battery_level=" + this.getBattery_level() +
                ", smoke=" + this.getSmoke() +
                ", gas=" + this.getGas() +
                ", temperature=" + this.getTemp() +
                ", uv=" + this.getUv() +
                '}';
    }

}