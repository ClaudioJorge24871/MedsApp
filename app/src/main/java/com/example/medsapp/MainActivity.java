package com.example.medsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //----------------UI Elements----------------------//
    private Button add_dispositivo_btn;
    private LinearLayout deviceContainer;


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

        //----------Constructors

        mqttHandler = new MQTTHandler();
        //mqttHandler.connect(BROKER_URL,CLIENT_ID);

        add_dispositivo_btn = findViewById(R.id.addDispBTN);
        deviceContainer = findViewById(R.id.linearLayout);

        // Load all saved devices
        List<Device> devices = DeviceStorage.getDevices(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (final Device d : devices) {
            View card = inflater.inflate(R.layout.device_card, deviceContainer, false);

            TextView tvTitle = card.findViewById(R.id.tvDeviceTitle);
            TextView tvDesc  = card.findViewById(R.id.tvDeviceDescription);

            tvTitle.setText(d.getTitle());
            tvDesc.setText(d.getDescription());

            // Optional: click on the card to go to details or subscribe to MQTT
            card.setOnClickListener(v -> {
                // e.g. open details or publish/subscribe:
                // publishMessage("medbox/" + d.getId() + "/cmd", "status");
                Toast.makeText(this, "Clicked “" + d.getTitle() + "”", Toast.LENGTH_SHORT).show();
            });

            deviceContainer.addView(card);
        }


        // In the future, subscribes to all topics set by the raspberry
        //mqttHandler.subscribe(topico);

        //Button listener
        add_dispositivo_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNewDevice.class);
                startActivity(intent);
            }
        });


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