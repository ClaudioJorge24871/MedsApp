package com.example.medsapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String BROKER_URL = "tcp://192.168.1.186:1883";    // Broker URl for connection
    private static final String CLIENT_ID = "claudRPI";                     // Client Id
    private MQTTHandler mqttHandler;                                        // Mqtt object

    /**
     * On app creation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        mqttHandler = new MQTTHandler();
        mqttHandler.connect(BROKER_URL,CLIENT_ID);
        // In the future, subscribes to all topics set by the raspberry
        //mqttHandler.subscribe(topico);
    }

    /**
     * Publish a message topic
     * @param topic
     * @param message
     */
    private void publishMessage(String topic, String message){
        mqttHandler.publish(topic,message);
    }

    /**
     * Method to subscribe to a topic
     * @param topic
     */
    private void subscribeToTopic(String topic){
        mqttHandler.subscribe(topic);
    }


    /**
     * On destroying the instance of the app
     */
    @Override
    protected void onDestroy() {
        mqttHandler.disconnect(); //disconnects the mqtt instance before destroying the app
        super.onDestroy();
    }
}