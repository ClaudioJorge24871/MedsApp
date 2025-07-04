package com.example.medsapp;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Client for Paho MQTT
 */
public class MQTTHandler {

    // Client creation
    private MqttClient client;

    /**
     * Connection method
     * @param brokerUrl
     * @param clientId
     */
    public void connect(String brokerUrl, String clientId){
        try{
            // Set up the persistence layer
            MemoryPersistence persistence = new MemoryPersistence();

            // Initialize the MQTT client
            client = new MqttClient(brokerUrl, clientId, persistence);

            //Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            //Connect the broker
            client.connect(connectOptions);

        }catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Disconnection method
     */
    public void disconnect(){
        try{
            client.disconnect();
        }catch (MqttException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Publish method
     * @param topic
     * @param message
     */
    public void publish(String topic, String message){
        try{
            MqttMessage mqttMsg = new MqttMessage(message.getBytes());
            client.publish(topic, mqttMsg);
        }catch (MqttException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Subscribe method
     * @param topic
     */
    public void subscribe (String topic){
        try{
            client.subscribe(topic);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
