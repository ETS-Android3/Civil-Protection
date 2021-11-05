package com.uoa.backend;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class BackEndApplication {

    public static void main(String[] args) throws IOException, ParserConfigurationException {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(BackEndApplication.class).run(args);

        String inputPath = "inputFiles";
        String outputPath = "outputFiles";
        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);

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

        // Create Mqtt client and connect to broker
        MqttConnection connection = new MqttConnection();

        // Example publish
        connection.publish("test", "hello world!");
    }

    public void onMessageArrived(String topic, MqttMessage message) {
        System.out.println("Received \"" + message + "\" in " + topic);
    }

}