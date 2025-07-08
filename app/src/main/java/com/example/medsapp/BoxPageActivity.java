package com.example.medsapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class BoxPageActivity extends AppCompatActivity {

    //----------------MQTT Elements----------------------//
    private static final String BROKER_URL = "tcp://medsboxbroker.duckdns.org:1883";    // Broker URl for connection
    private static final String CLIENT_ID = "claudRPI";                     // Client Id
    private MQTTHandler mqttHandler;                                        // Mqtt object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_box_page);

        connectToBroker(); // Connect to broker

        TextView boxNameTitle = findViewById(R.id.boxNameView);
        EditText descText = findViewById(R.id.descricaoCaixaText);
        EditText identText = findViewById(R.id.identificadorCaixaText);

        String title = getIntent().getStringExtra("device_title");
        String id = getIntent().getStringExtra("device_id");
        String desc = getIntent().getStringExtra("device_description");

        boxNameTitle.setText(title);
        descText.setText(desc);
        identText.setText(id);

        // Send message to open the broker to the broker
        Button openBox = findViewById(R.id.abrirCaixaBTN);
        openBox.setOnClickListener(v -> {
            String message = "Abre caixa de: " + id;            // Change this message in the future
            String topic = "medsbox/" + id + "/abrecaixa";
            publishMessage(topic, message);
        });


        // Go back arrow
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());
    }

    /**
     * Initiate broker connection
     */
    private void connectToBroker(){
        mqttHandler = new MQTTHandler();
        mqttHandler.connect(BROKER_URL,CLIENT_ID);
    }


    /**
     * Publish a message topic
     * @param topic
     * @param message
     */
    public void publishMessage(String topic, String message){
        Toast.makeText(this, "Publishing message:" + message, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic,message);
    }

    /**
     * Method to subscribe to a topic
     * @param topic
     */
    public void subscribeToTopic(String topic){
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