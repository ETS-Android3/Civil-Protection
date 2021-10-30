import com.opencsv.exceptions.CsvValidationException;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) throws ParserConfigurationException, IOException, CsvValidationException, SQLException, MqttException {

        String inputPath = "inputFiles";
        String outputPath = "outputFiles";
        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);
        // Topics to listen to
        String clientTopics = "civil/clients/#";
        // Topics to write to
        String serverTopics = "civil/server/#";

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

        // Produce output files
        Parser parser = new Parser();
        parser.parseXmlToCsv(inputPath + "/android_1.xml");
        parser.parseXmlToCsv(inputPath + "/android_2.xml");

        // Database demo
        DatabaseHelper db = new DatabaseHelper();
        db.connect();

        // MQTT broker connection demo
        MqttAsyncClient connection = new MqttAsyncClient("tcp://localhost:1883", String.valueOf(new Random().nextInt(10000)), new MemoryPersistence());
        connection.setCallback(new MqttCallback());

        // Connect to broker
        try {
            IMqttToken token = connection.connect();
            token.waitForCompletion();
            System.out.println("Successfully connected to broker!");
        } catch (MqttException e) {
            System.out.println("Failed to connect to broker!");
            db.disconnect();
            System.exit(-1);
        }

        // Subscribe to all client topics
        try {
            connection.subscribe("civil/clients/#", 2);
            System.out.println("Successfully subscribed to " + clientTopics);
        } catch (MqttException e) {
            System.out.println("Failed to subscribe to " + clientTopics);
            e.getMessage();
        }

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

}