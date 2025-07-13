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

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BoxPageActivity extends AppCompatActivity {

    TextView LogsBox;

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
        LogsBox = findViewById(R.id.logsTextBox);

        String title = getIntent().getStringExtra("device_title");
        String id = getIntent().getStringExtra("device_id");
        String desc = getIntent().getStringExtra("device_description");

        boxNameTitle.setText(title);
        descText.setText(desc);
        identText.setText(id);

        // Send message to open the broker to the broker
        Button openBox = findViewById(R.id.abrirCaixaBTN);

        //MQTT topic for this box
        String topicPub = "medsbox/" + id + "/mybox";

        // Set a listener to send the JSON message for the specified topic
        openBox.setOnClickListener(v -> {
            // Building MQTT Json message
            String message = buildJSONMessage("openbox", id).toString();
            publishMessage(topicPub, message);

            //openBox.setEnabled(false);
        });

        //Subscribes to the LOGS topic
        // The logs topic is where the RPI will send the states of the box
        String topicSub = "medsbox/" + id + "/logs";
        subscribeToTopic(topicSub);

        // Subscribes to the LWT topic
        String lwttopic = "medsbox/" + id + "/rpilwt";
        subscribeToTopic(lwttopic);

        mqttHandler.client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                runOnUiThread(() -> Toast.makeText(BoxPageActivity.this, "MQTT connection lost", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                changeLogBoxText(message);

                //if (message.toString().equals("A caixa foi fechada")){
                //    openBox.setEnabled(true);
                //}
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        // Go back arrow
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());
    }

    /**
     * Build the JSON message for raspberry to unload it
     * @param command - Instruction for raspberry (open box or close box)
     * @return
     */
    private JSONObject buildJSONMessage(String command, String id){
        JSONObject json = new JSONObject();
        try{
            json.put("id",id);
            json.put("command", command);
            json.put("dayofweek",getDayOfWeek());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    /**
     * Change the text view of Logs in view to the messages
     * @param message
     */
    private void changeLogBoxText(MqttMessage message){
        // get the old text
        String oldText = LogsBox.getText().toString();

        // Gets the hour of the message
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentTime = sdf.format(new Date());

        //Builds the new text
        String newText = oldText + "\n[" + currentTime + "] " + message.toString();

        LogsBox.setText(newText);
    }

    /**
     * Returns the current day of week
     * @return 1 = sunday, 2 = monday ... 7 = saturday
     */
    private int getDayOfWeek(){
        Calendar calendar = Calendar.getInstance();
        int curWeekDay = calendar.get(Calendar.DAY_OF_WEEK); // 1 = sunday, 2 = monday .. 7 = saturday
        return curWeekDay;
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