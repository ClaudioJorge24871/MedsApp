package com.example.medsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import android.widget.Toast;

public class AddNewDevice extends AppCompatActivity {

    private Button addDeviceButton;
    private EditText deviceTitle;
    private EditText deviceId;
    private EditText deviceDescription;

    private ConnectivityManager.NetworkCallback internetNetworkCallback;

    private ConnectivityManager connectivityManager;
    private WebView setupWebView;

    private Button hideButton;

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

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        addDeviceButton = findViewById(R.id.addNewDeviceBTN);
        deviceId = findViewById(R.id.IdentificadorText);
        deviceTitle = findViewById(R.id.tituloText);
        deviceDescription = findViewById(R.id.desText);

        setupWebView = findViewById(R.id.setupWebView);

        hideButton = findViewById(R.id.hideWebViewButton);

        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideButton.setEnabled(false);

                // Unbind from ESP-CONFIG network immediately
                connectivityManager.bindProcessToNetwork(null);

                // Request a network with internet (automatically excludes ESP-CONFIG)
                NetworkRequest normalRequest = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build();

                internetNetworkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        runOnUiThread(() -> {
                            // Hide WebView immediately
                            setupWebView.setVisibility(View.GONE);
                            hideButton.setVisibility(View.INVISIBLE);

                            // Start loading screen
                            connectToNetworkAndBroker();
                        });
                        unregisterInternetNetworkCallback(); // Cleanup callback
                    }

                    @Override
                    public void onUnavailable() {
                        runOnUiThread(() -> {
                            Toast.makeText(AddNewDevice.this, "No internet network found", Toast.LENGTH_SHORT).show();
                            hideButton.setEnabled(true);
                        });
                        unregisterInternetNetworkCallback(); // Cleanup callback
                    }
                };

                // Register network request
                connectivityManager.requestNetwork(normalRequest, internetNetworkCallback);

                // Add timeout (e.g., 15 seconds)
                new Handler().postDelayed(() -> {
                    if (internetNetworkCallback != null) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddNewDevice.this, "Network search timed out", Toast.LENGTH_SHORT).show();
                            hideButton.setEnabled(true);
                        });
                        unregisterInternetNetworkCallback();
                    }
                }, 15000);
            }
        });


        addDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (deviceId.getText().toString().trim().isEmpty()){
                    deviceId.setError("Obrigatório");
                    return;
                }

                WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                        .setSsid("ESP-CONFIG")
                        .setWpa2Passphrase("12345678")
                        .build();

                NetworkRequest request = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier)
                        .build();

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        connectivityManager.bindProcessToNetwork(network);

                        runOnUiThread(() -> {
                            setupWebView.getSettings().setJavaScriptEnabled(true);
                            setupWebView.setVisibility(View.VISIBLE);
                            hideButton.setVisibility(View.VISIBLE);
                            setupWebView.setWebViewClient(new WebViewClient());
                            setupWebView.loadUrl("http://192.168.4.1");
                        });

                    }

                    @Override
                    public void onLost(Network network) {
                        connectivityManager.bindProcessToNetwork(null);
                    }
                };

                connectivityManager.requestNetwork(request, networkCallback);


            }
        });

        // Go back arrow
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());
    }

    private void unregisterInternetNetworkCallback() {
        if (internetNetworkCallback != null) {
            connectivityManager.unregisterNetworkCallback(internetNetworkCallback);
            internetNetworkCallback = null;
        }
    }

    public void connectToNetworkAndBroker(){

        loadingLayout = findViewById(R.id.loadingLayout);
        TextView semRedeLabel = findViewById(R.id.textView3);
        loadingLayout.setVisibility(View.VISIBLE);
        semRedeLabel.setText("A tentar ligar a caixa à rede... \n Isto pode demorar algum tempo, por favor não saia da aplicação");

        connectToBroker();

        if (deviceId.getText().toString().trim().isEmpty()) {
            deviceId.setError("Obrigatório");
        }

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
                    deviceId.setError("Obrigatório");
                    deviceTitle.setError("Obrigatório");
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
        unregisterInternetNetworkCallback();
        if (mqttHandler != null) {
            mqttHandler.disconnect();
        }
        super.onDestroy();
    }
}