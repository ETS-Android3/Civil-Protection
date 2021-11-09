package com.uoa.backend;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttConnection {

    private final MqttAsyncClient client;
    private static final String brokerURI = "tcp://localhost:1883";
    private static final String sessionID = "Civil-Protection-Edge-Server";
    // Topics to listen to
    private static final String clientTPrefix = "civil/clients/";
    private static final String sensorTPrefix = "civil/sensors/";
    // Topics to write to
    private static final String sToClientsPrefix = "civil/server/";
    /**** Not to be used ****/
    private final String sToSensorsPrefix = "civil/server-sensors/";

    public MqttConnection() {
        client = connect(brokerURI, sessionID, new MemoryPersistence());
        client.setCallback(new MqttCallback());
        // Subscribe to all client topics
        subscribe(clientTPrefix + "#");
        // Subscribe to all sensor topics
        subscribe(sensorTPrefix + "#");
    }

    private MqttAsyncClient connect(String uri, String sessionID, MemoryPersistence persistence) {
        MqttAsyncClient client = null;
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{uri});
        options.setAutomaticReconnect(true);
        try {
            client = new MqttAsyncClient(uri, sessionID, persistence);
            IMqttToken token = client.connect();
            token.waitForCompletion();
            System.out.println("Successfully connected to broker!");
        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to broker!");
        }
        return client;
    }

    public void subscribe(String topic) {
        if (client.isConnected()) {
            try {
                client.subscribe(topic, 2);
                System.out.println("Subscribed to " + topic);
            } catch (MqttException e) {
                System.out.println("Failed to subscribe to " + topic);
                e.getMessage();
            }
        } else {
            System.out.println("Connection is lost!");
        }
    }

    public void publish(String topic, String message) {
        MqttMessage msg = new MqttMessage(message.getBytes());
        msg.setQos(2);
        String fixedTopic = sToClientsPrefix + topic;
        if (client.isConnected()) {
            try {
                client.publish(fixedTopic, msg);
                System.out.println("Published \"" + message + "\" to " + fixedTopic);
            } catch (MqttException e) {
                e.printStackTrace();
                System.out.println("Failed to publish to " + fixedTopic);
            }
        } else {
            System.out.println("Connection is lost!");
        }
    }

}