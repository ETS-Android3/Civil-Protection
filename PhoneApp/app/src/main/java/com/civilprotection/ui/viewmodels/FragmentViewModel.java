package com.civilprotection.ui.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FragmentViewModel extends ViewModel {

    // Publish Fragment
    private final MutableLiveData<String> publishTopic = new MutableLiveData<>();
    private final MutableLiveData<String> publishMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> publishQos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> publishRetain = new MutableLiveData<>();
    // Subscribe Fragment
    private final MutableLiveData<String> subscribeTopic = new MutableLiveData<>();
    private final MutableLiveData<Integer> subscribeQos = new MutableLiveData<>();
    // Simulation Fragment
    private final MutableLiveData<String> simulationFilePath = new MutableLiveData<>();
    private final MutableLiveData<Integer> simulationQos = new MutableLiveData<>();
    private final MutableLiveData<String> simulationTimeOut = new MutableLiveData<>();
    private final MutableLiveData<Boolean> simulationRetain = new MutableLiveData<>();

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