package com.uoa.server.handleinput;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

// Simple XML to CSV parser
public class Parser {

    public Parser(String fileName) {
        File inputDir = new File("inputFiles");
        File outputDir = new File("outputFiles");
        File file = new File(inputDir + "/" + fileName);

        if (!file.exists()) System.out.println("Cannot find input files. Aborting...");

        if (!outputDir.exists())
            if (!outputDir.mkdir()) System.out.println("Could not create output directory");

        try {
            parseXmlToCsv(inputDir + "/" + fileName);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private static String initFile(String filePath) {
        String fileType = null;
        final File file = new File(filePath);
        try {
            fileType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            System.out.println("Unknown file type");
            e.printStackTrace();
        }
        assert fileType != null;
        if (fileType.equals("text/xml")) {
            filePath = filePath.substring(0, filePath.length() - 4);
            if (filePath.contains("/")) {
                filePath = filePath.substring(filePath.lastIndexOf("/") + 1);
            }
        }
        return "outputFiles/" + filePath + ".csv";
    }

    // Converts $inFile from XML to CSV format
    private static void parseXmlToCsv(String inFile) throws ParserConfigurationException {

        String outFile = initFile(inFile);

        // Build Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = null;
        try {
            document = builder.parse(new File(inFile));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        assert document != null;
        document.getDocumentElement().normalize();

        // Track all records info by timesteps
        NodeList nodeList = document.getElementsByTagName("timestep");

        try (FileWriter writer = new FileWriter(outFile)) {

            for (int record = 0; record < nodeList.getLength(); record++) {

                Node node = nodeList.item(record);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    if (node.hasAttributes()) {
                        // Get attributes names and values
                        NamedNodeMap nodeMap = node.getAttributes();
                        String timestep = "";
                        for (int i = 0; i < nodeMap.getLength(); i++) {
                            Node currentNode = nodeMap.item(i);
                            if (currentNode.getNodeName().equals("time")) {
                                timestep = currentNode.getNodeValue();
                            }
                        }

                        // Build a record to store all the info at a specific timestep
                        ClientRecord vRecord = new ClientRecord();
                        vRecord.setTimestep(timestep);

                        // Now read children nodes
                        if (node.hasChildNodes()) {

                            NodeList childNodeList = node.getChildNodes();
                            for (int vehicle = 0; vehicle < childNodeList.getLength(); vehicle++) {

                                Node childNode = childNodeList.item(vehicle);
                                if (childNode.getNodeType() == childNode.ELEMENT_NODE) {

                                    if (childNode.hasAttributes()) {

                                        // Get attributes names and values
                                        NamedNodeMap childNodeMap = childNode.getAttributes();

                                        for (int j = 0; j < childNodeMap.getLength(); j++) {

                                            Node tempNode = childNodeMap.item(j);
                                            switch (tempNode.getNodeName()) {
                                                case "id":
                                                    vRecord.setId(tempNode.getNodeValue());
                                                    break;
                                                case "y":
                                                    vRecord.setLatitude(tempNode.getNodeValue());
                                                    break;
                                                case "x":
                                                    vRecord.setLongitude(tempNode.getNodeValue());
                                                    break;
                                                case "angle":
                                                    vRecord.setAngle(tempNode.getNodeValue());
                                                    break;
                                                case "type":
                                                    vRecord.setType(tempNode.getNodeValue());
                                                    break;
                                                case "speed":
                                                    vRecord.setSpeed(tempNode.getNodeValue());
                                                    break;
                                                case "pos":
                                                    vRecord.setPosition(tempNode.getNodeValue());
                                                    break;
                                                case "lane":
                                                    vRecord.setLane(tempNode.getNodeValue());
                                                    break;
                                                case "slope":
                                                    vRecord.setSlope(tempNode.getNodeValue());
                                                    break;
                                                default:
                                                    break;
                                            }

                                        }

                                        // Store the final information to the output file
                                        writer.append(vRecord.getLatitude()).append(",");
                                        writer.append(vRecord.getLongitude()).append("\n");

                                    }

                                }

                            }

                        }

                    }

                }

            }

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}