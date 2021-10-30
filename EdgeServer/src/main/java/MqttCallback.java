import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCallback extends Server implements org.eclipse.paho.client.mqttv3.MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection was lost!");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        onMessageArrived(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            System.out.println("Message " + token.getMessage().toString() + " was delivered!");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
