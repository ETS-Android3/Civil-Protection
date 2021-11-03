import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SQLException, MqttException {

        String inputPath = "inputFiles";
        String outputPath = "outputFiles";
        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);

        // Topics to listen to
        String clientTPrefix = "civil/clients/";
        String sensorTPrefix = "civil/sensors/";
        // Topics to write to
        String sToClientsPrefix = "civil/server/";
        /* *** Not to be used *** */
        String sToSensorsPrefix = "civil/server-sensors/";

        if (!inputDir.exists()) throw new IOException("Cannot find input files. Aborting...");

        if (!outputDir.exists()) {
            if (!outputDir.mkdir()) throw new IOException("Could not create output directory");
        } else {
            System.out.println("Old output files detected. Clearing up...");
            File[] entries = outputDir.listFiles();
            if (entries != null)
                for (File entry : entries)
                    if (!entry.delete()) throw new IOException("Failed to delete file " + entry.getName());
        }

        // Convert xml files to csv
        Parser.parseXmlToCsv(inputPath + "/android_1.xml");
        Parser.parseXmlToCsv(inputPath + "/android_2.xml");

        // Database demo
        DatabaseHelper db = new DatabaseHelper();
        db.connect();

        // Create Mqtt client and connect to broker
        MqttAsyncClient connection = connect("tcp://localhost:1883", String.valueOf(new Random().nextInt(10000)), new MemoryPersistence());
        if (connection == null) {
            db.disconnect();
            System.exit(-1);
        }
        connection.setCallback(new MqttCallback());

        // Subscribe to all client topics
        subscribe(connection, clientTPrefix + "#");

        // Subscribe to all sensor topics
        subscribe(connection, sensorTPrefix + "#");

        // Example publish
        publish(connection, sToClientsPrefix + "test", "hello world!");

        System.out.println("Type \"shutdown\" at any time to shut down the Edge-Server");
        Scanner reader = new Scanner(System.in);
        // Keep the server running until user asks to stop
        while (!reader.nextLine().equals("shutdown"));

        // Disconnect Database
        db.disconnect();
        if (connection.isConnected()) connection.disconnect();
    }

    public void onMessageArrived(String topic, MqttMessage message) {
        System.out.println("Received: \"" + message + "\" in topic " + topic);
    }

    private static MqttAsyncClient connect(String uri, String sessionID, MemoryPersistence persistence) {
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

    private static void subscribe(MqttAsyncClient connection, String topic) {
        try {
            connection.subscribe(topic, 2);
            System.out.println("Successfully subscribed to " + topic);
        } catch (MqttException e) {
            System.out.println("Failed to subscribe to " + topic);
            e.getMessage();
        }
    }

    private static void publish(MqttAsyncClient connection, String topic, String message) {
        MqttMessage msg = new MqttMessage(message.getBytes());
        msg.setQos(2);
        try {
            connection.publish(topic, msg);
            System.out.println("Published \"" + message + "\" to " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("Failed to publish to " + topic);
        }
    }

}