package com.uoa.backend;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCallback extends BackEndApplication implements org.eclipse.paho.client.mqttv3.MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        onMessageArrived(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
