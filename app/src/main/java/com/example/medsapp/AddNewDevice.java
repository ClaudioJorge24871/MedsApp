package com.example.medsapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.os.Handler;

public class AddNewDevice extends AppCompatActivity {

    private Button addDeviceButton;
    private EditText deviceTitle;
    private EditText deviceId;
    private EditText deviceDescription;

    private LinearLayout loadingLayout;

    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    //----------------MQTT Elements----------------------//
    private static final String BROKER_URL = "tcp://medsboxbroker.duckdns.org:1883";    // Broker URl for connection
    private static final String CLIENT_ID = "claudRPI";                     // Client Id
    private MQTTHandler mqttHandler;                                        // Mqtt object


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_new_device);

        addDeviceButton = findViewById(R.id.addNewDeviceBTN);
        deviceId = findViewById(R.id.IdentificadorText);
        deviceTitle = findViewById(R.id.tituloText);
        deviceDescription = findViewById(R.id.desText);

        addDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Open the browser to insert the SSID and Password of the desired network
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(android.net.Uri.parse("http://192.168.4.1"));
                startActivity(browserIntent);

            }
        });

        // Go back arrow
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());
    }

    @Override
    public void onRestart(){
        super.onRestart();

        loadingLayout = findViewById(R.id.loadingLayout);
        TextView semRedeLabel = findViewById(R.id.textView3);
        loadingLayout.setVisibility(View.VISIBLE);
        semRedeLabel.setText("A tentar ligar a caixa à rede... \n Isto pode demorar algum tempo, por favor não saia da aplicação");

        connectToBroker();

        String topicSub = "medsbox/" + deviceId.getText().toString().trim() + "/statusNet";
        subscribeToTopic(topicSub);

        mqttHandler.client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(message.toString().equals("connected")){
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    loadingLayout.setVisibility(View.INVISIBLE);
                    semRedeLabel.setText("A caixa foi conectada com sucesso!");
                    addDeviceButton.setText("Guardar");
                    addListenertoButton(addDeviceButton);
                }else{
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    semRedeLabel.setText("Não foi possivel conectar a caixa à rede desejada. Por favor tente novamente");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                loadingLayout.setVisibility(View.INVISIBLE);
                semRedeLabel.setText("Tempo de espera esgotado. Não foi possível ligar a caixa à rede. Por favor tente novamente");
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 60 * 1000);
    }


    /**
     * Initiate broker connection
     */
    private void connectToBroker(){
        mqttHandler = new MQTTHandler();
        mqttHandler.connect(BROKER_URL,CLIENT_ID);
    }

    /**
     * Method to subscribe to a topic
     * @param topic
     */
    public void subscribeToTopic(String topic){
        mqttHandler.subscribe(topic);
    }


    private void addListenertoButton(Button addDeviceButton){
        addDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                // When the button to add a new device is clicked. Store the new device
                //Gets the device info
                String id = deviceId.getText().toString().trim();
                String title = deviceTitle.getText().toString().trim();
                String description = deviceDescription.getText().toString().trim();

                if(id.isEmpty() || title.isEmpty()){
                    deviceId.setError("Required");
                    deviceId.setError("Required");
                    return;
                }

                // Stores the new device
                Device newDevice = new Device(id, title, description);
                DeviceStorage.addDevice(AddNewDevice.this, newDevice);

                //Back to the home page
                Intent intent = new Intent(AddNewDevice.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * On destroying the instance of the app
     */
    @Override
    protected void onDestroy() {
        if (mqttHandler != null) {
            mqttHandler.disconnect();
        }
        super.onDestroy();
    }
}