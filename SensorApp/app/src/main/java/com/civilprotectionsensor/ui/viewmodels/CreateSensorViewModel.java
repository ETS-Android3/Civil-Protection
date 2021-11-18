package com.civilprotectionsensor.ui.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateSensorViewModel extends ViewModel {

    // Smoke sensor
    private final MutableLiveData<Float> smokeMin = new MutableLiveData<>();
    private final MutableLiveData<Float> smokeMax = new MutableLiveData<>();
    // Gas sensor
    private final MutableLiveData<Float> gasMin = new MutableLiveData<>();
    private final MutableLiveData<Float> gasMax = new MutableLiveData<>();
    // Temperature sensor
    private final MutableLiveData<Float> tempMin = new MutableLiveData<>();
    private final MutableLiveData<Float> tempMax = new MutableLiveData<>();
    // Uv sensor
    private final MutableLiveData<Float> uvMin = new MutableLiveData<>();
    private final MutableLiveData<Float> uvMax = new MutableLiveData<>();

    public void setSmokeMin(Float value) {
        smokeMin.setValue(value);
    }

    public MutableLiveData<Float> getSmokeMin() {
        return smokeMin;
    }

    public void setSmokeMax(Float value) {
        smokeMax.setValue(value);
    }

    public MutableLiveData<Float> getSmokeMax() {
        return smokeMax;
    }

    public void setGasMin(Float value) {
        gasMin.setValue(value);
    }

    public MutableLiveData<Float> getGasMin() {
        return gasMin;
    }

    public void setGasMax(Float value) {
        gasMax.setValue(value);
    }

    public MutableLiveData<Float> getGasMax() {
        return gasMax;
    }

    public void setTempMin(Float value) {
        tempMin.setValue(value);
    }

    public MutableLiveData<Float> getTempMin() {
        return tempMin;
    }

    public void setTempMax(Float value) {
        tempMax.setValue(value);
    }

    public MutableLiveData<Float> getTempMax() {
        return tempMax;
    }

    public void setUvMin(Float value) {
        uvMin.setValue(value);
    }

    public MutableLiveData<Float> getUvMin() {
        return uvMin;
    }

    public void setUvMax(Float value) {
        uvMax.setValue(value);
    }

    public MutableLiveData<Float> getUvMax() {
        return uvMax;
    }

}