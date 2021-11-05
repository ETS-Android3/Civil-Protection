package com.civilprotection.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ItemViewModel extends ViewModel {

    // Publish Fragment
    private final MutableLiveData<String> publishTopic = new MutableLiveData<String>();
    private final MutableLiveData<String> publishMessage = new MutableLiveData<String>();
    private final MutableLiveData<Integer> publishQos = new MutableLiveData<Integer>();
    private final MutableLiveData<Boolean> publishRetain = new MutableLiveData<Boolean>();
    // Subscribe Fragment
    private final MutableLiveData<String> subscribeTopic = new MutableLiveData<String>();
    private final MutableLiveData<Integer> subscribeQos = new MutableLiveData<Integer>();
    // Simulation Fragment
    private final MutableLiveData<String> simulationFilePath = new MutableLiveData<String>();
    private final MutableLiveData<Integer> simulationQos = new MutableLiveData<Integer>();
    private final MutableLiveData<String> simulationTimeOut = new MutableLiveData<String>();
    private final MutableLiveData<Boolean> simulationRetain = new MutableLiveData<Boolean>();

    // =============== Publish Fragment ===============
    public void setPublishTopic(String value) {
        publishTopic.setValue(value);
    }

    public MutableLiveData<String> getPublishTopic() {
        return publishTopic;
    }

    public void setPublishMessage(String value) {
        publishMessage.setValue(value);
    }

    public MutableLiveData<String> getPublishMessage() {
        return publishMessage;
    }

    public void setPublishQos(Integer value) {
        publishQos.setValue(value);
    }

    public MutableLiveData<Integer> getPublishQos() {
        return publishQos;
    }

    public void setPublishRetain(Boolean value) {
        publishRetain.setValue(value);
    }

    public MutableLiveData<Boolean> getPublishRetain() {
        return publishRetain;
    }

    // =============== Subscribe Fragment ===============
    public void setSubscribeTopic(String value) {
        subscribeTopic.setValue(value);
    }

    public MutableLiveData<String> getSubscribeTopic() {
        return subscribeTopic;
    }

    public void setSubscribeQos(Integer value) {
        subscribeQos.setValue(value);
    }

    public MutableLiveData<Integer> getSubscribeQos() {
        return subscribeQos;
    }

    // =============== Simulation Fragment ===============
    public void setSimulationFilePath(String value) {
        simulationFilePath.setValue(value);
    }

    public MutableLiveData<String> getSimulationFilePath() {
        return simulationFilePath;
    }

    public void setSimulationQos(Integer value) {
        simulationQos.setValue(value);
    }

    public MutableLiveData<Integer> getSimulationQos() {
        return simulationQos;
    }

    public void setSimulationTimeOut(String value) {
        simulationTimeOut.setValue(value);
    }

    public MutableLiveData<String> getSimulationTimeOut() {
        return simulationTimeOut;
    }

    public void setSimulationRetain(Boolean value) {
        simulationRetain.setValue(value);
    }

    public MutableLiveData<Boolean> getSimulationRetain() {
        return simulationRetain;
    }

}