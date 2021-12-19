package com.civilprotectionsensor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CallbackHandler extends Service implements MqttCallbackExtended {

    public interface CallBackListener {
        void onMessageArrived(String topic, MqttMessage message);

        void onConnectionLost();

        void onReconnected();
    }

    CallBackListener listener;

    CallbackHandler(CallBackListener callBackListener) {
        this.listener = callBackListener;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) listener.onReconnected();
    }

    @Override
    public void connectionLost(Throwable cause) {
        listener.onConnectionLost();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        listener.onMessageArrived(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}