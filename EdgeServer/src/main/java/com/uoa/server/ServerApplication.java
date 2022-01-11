package com.uoa.server;

import com.uoa.server.connection.MqttConnection;
import com.uoa.server.handleinput.Parser;
import com.uoa.server.registry.EventEntry;
import com.uoa.server.registry.EventEntryService;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

@SpringBootApplication
public class ServerApplication {

    private static final MqttConnection connection = new MqttConnection();
    private static final Parser inputFile1 = new Parser("android_1.xml");
    private static final Parser inputFile2 = new Parser("android_2.xml");
    public static DevicesList androidDevices = new DevicesList();
    public static DevicesList iotDevices = new DevicesList();
    private static EventEntryService eventEntryService;
    public static final int coolDown = 30000; // 30s coolDown
    public HashMap<String, Long> coolDownMap = new HashMap<>();

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ServerApplication.class, args);
        eventEntryService = applicationContext.getBean(EventEntryService.class);
    }

    public void onMessageArrived(String topic, MqttMessage message) {
        String[] topicSplit = topic.split("/");
        if (topicSplit.length == 4) {
            String topicType = topicSplit[1];
            Integer deviceId = Integer.parseInt(topicSplit[2]);
            // Find out the type of the device
            if (topicType.compareTo("clients") == 0) {
                parseAndroidDevice(deviceId, message);
            } else if (topicType.compareTo("sensors") == 0) {
                parseIoTDevice(deviceId, message);
            } else {
                System.out.println("Unknown device type detected");
            }
        }
    }

    public void parseAndroidDevice(Integer deviceId, MqttMessage message) {
        String[] splitMessage = message.toString().split(",");
        if (splitMessage.length == 2) {
            try {
                double lat = Double.parseDouble(splitMessage[0]);
                double lng = Double.parseDouble(splitMessage[1]);
                androidDevices.add(new AndroidDevice(deviceId, lat, lng, System.currentTimeMillis() / 1000));
            } catch (NumberFormatException exception) {
                System.err.println("Error parsing android device. Miss-formatted data. Double parsing failed.");
            }
        }
    }

    public void parseIoTDevice(Integer deviceId, MqttMessage message) {
        String[] splitMessage = message.toString().split(";");
        if (splitMessage.length >= 3) {
            String[] deviceInfo = Arrays.copyOfRange(splitMessage, 0, 3);
            String[] sensorInfo = Arrays.copyOfRange(splitMessage, 3, splitMessage.length);

            try {
                double lat = Double.parseDouble(deviceInfo[0]);
                double lng = Double.parseDouble(deviceInfo[1]);
                double battery = Double.parseDouble(deviceInfo[2]);
                IoTDevice iotDevice = new IoTDevice(deviceId, lat, lng, battery, System.currentTimeMillis() / 1000);
                // These flags indicates if this IoT device has multiple sensors of the same type
                boolean extraSmoke = false, extraGas = false, extraTemp = false, extraUv = false;
                // sValue holds either the sensor value or the medium of the same-type sensors' values
                double sValue;

                for (int param = 0; (param < sensorInfo.length) && (param + 1 < sensorInfo.length); param += 2) {
                    switch (sensorInfo[param]) {
                        case "smoke":
                            if (extraSmoke) sValue = (Double.parseDouble(sensorInfo[param + 1]) + iotDevice.getSmoke()) / 2;
                            else {
                                sValue = (Double.parseDouble(sensorInfo[param + 1]));
                                extraSmoke = true;
                            }
                            iotDevice.setSmoke(sValue);
                            break;
                        case "gas":
                            if (extraGas) sValue = (Double.parseDouble(sensorInfo[param + 1]) + iotDevice.getGas()) / 2;
                            else {
                                sValue = (Double.parseDouble(sensorInfo[param + 1]));
                                extraGas = true;
                            }
                            iotDevice.setGas(sValue);
                            break;
                        case "temp":
                            if (extraTemp) sValue = (Double.parseDouble(sensorInfo[param + 1]) + iotDevice.getTemp()) / 2;
                            else {
                                sValue = (Double.parseDouble(sensorInfo[param + 1]));
                                extraTemp = true;
                            }
                            iotDevice.setTemp(sValue);
                            break;
                        case "uv":
                            if (extraUv) sValue = (Double.parseDouble(sensorInfo[param + 1]) + iotDevice.getUv()) / 2;
                            else {
                                sValue = (Double.parseDouble(sensorInfo[param + 1]));
                                extraUv = true;
                            }
                            iotDevice.setUv(sValue);
                            break;
                        default:
                            System.err.println("Unknown parameter: " + sensorInfo[param]);
                            break;
                    }
                }
                iotDevices.add(iotDevice);

                // Check if this IoT device caused an event
                if (iotDevice.eventDetection() != 0) {
                    boolean alertHigh = false;
                    String alertMsg = null;
                    EventEntry eventEntry;
                    switch (iotDevice.eventDetection()) {
                        case 1:
                            alertMsg = IoTDevice.dangerMsg[0];
                            alertHigh = true;
                            break;
                        case 2:
                            alertMsg = IoTDevice.dangerMsg[1];
                            alertHigh = false;
                            break;
                        case 3:
                            alertMsg = IoTDevice.dangerMsg[2];
                            alertHigh = true;
                            break;
                        case 4:
                            alertMsg = IoTDevice.dangerMsg[3];
                            alertHigh = true;
                            break;
                        default:
                            break;
                    }

                    System.out.println("IoT device info: Smoke: " + iotDevice.getSmoke() + " Gas: " + iotDevice.getGas()
                            + " Temp: " + iotDevice.getTemp() + " UV: " + iotDevice.getUv());
                    if (isOnCoolDown(iotDevice.getDevice_id(), iotDevice.getLat(), iotDevice.getLng(), alertMsg)) {
                        System.out.println("Detected event that is on cool-down!");
                    } else {
                        eventEntry = new EventEntry(getCurrentTimeStamp(), iotDevice.getLat(), iotDevice.getLng(),
                            alertHigh ? IoTDevice.DANGER_LEVEL_HIGH : IoTDevice.DANGER_LEVEL_MEDIUM,
                            iotDevice.getSmoke(), iotDevice.getGas(), iotDevice.getTemp(), iotDevice.getUv(), alertMsg);
                        eventEntryService.save(eventEntry);
                        notifyAndroidDevices(iotDevice.getLat(), iotDevice.getLng(), alertHigh, alertMsg);
                    }
                }
            } catch (NumberFormatException exception) {
                System.err.println("Error parsing sensor. Miss-formatted data. Double parsing failed.");
            }

        }
    }

    public void notifyAndroidDevices(Double lat, Double lng, boolean severity, String message) {
        String level = severity ? "High" : "Medium";
        System.out.println("Danger detected! Danger level: " + level);
        System.out.println("Danger message: " + message);
        for (int client = 0; client < androidDevices.size(); client++) {
            Integer deviceId = androidDevices.get(client).getDevice_id();
            double distance = DistanceCalculator.distance(lat, lng, androidDevices.get(client).getLat(), androidDevices.get(client).getLng(), "K");
            String topic = deviceId + "/alerts";
            String alertMsg = severity + ";" + distance + ";" + message;
            System.out.println("Notifying android device " + deviceId + " which is " + distance + " kilometers away.");
            connection.publish(topic, alertMsg);
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat currentDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return currentDateTimeFormat.format(new Date());
    }

    public boolean isOnCoolDown(Integer devId, Double lat, Double lng, String msg) {
        String eventKey = devId + lat + lng + msg;
        if (coolDownMap.containsKey(eventKey)) {
            if (System.currentTimeMillis() - coolDownMap.get(eventKey) <= coolDown) return true;
            else {
                coolDownMap.remove(eventKey);
                return false;
            }
        } else coolDownMap.put(eventKey, System.currentTimeMillis());
        return false;
    }

}