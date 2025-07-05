package com.example.medsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    //----------------UI Elements----------------------//
    //private EditText editTextTopic;
    //private Button btnSubscribe;


    //----------------MQTT Elements----------------------//
    private static final String BROKER_URL = "tcp://192.168.1.186:1883";    // Broker URl for connection
    private static final String CLIENT_ID = "claudRPI";                     // Client Id
    private MQTTHandler mqttHandler;                                        // Mqtt object

    /**
     * On app creation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mqttHandler = new MQTTHandler();
        //mqttHandler.connect(BROKER_URL,CLIENT_ID);

        //editTextTopic = findViewById(R.id.editTextText2);
        //btnSubscribe = findViewById(R.id.PubTopicbutton);

        // In the future, subscribes to all topics set by the raspberry
        //mqttHandler.subscribe(topico);

        /**Button listener
        btnSubscribe.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = editTextTopic.getText().toString().trim();
                if(!message.isEmpty()){
                    publishMessage("testMqtt/topic",message);
                }
            }
        });*/
    }

    /**
     * Publish a message topic
     * @param topic
     * @param message
     */
    private void publishMessage(String topic, String message){
        Toast.makeText(this, "Publishing message:" + message, Toast.LENGTH_SHORT).show();
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