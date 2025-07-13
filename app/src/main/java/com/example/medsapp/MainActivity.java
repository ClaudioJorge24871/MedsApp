package com.example.medsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //----------------UI Elements----------------------//
    private Button add_dispositivo_btn;
    private LinearLayout deviceContainer;

    private TextView emptyLabel;

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

        String clientId = getOrCreateClientId(this);
        System.out.println("Identificador do cliente: " + clientId);

        //----------Constructors


        add_dispositivo_btn = findViewById(R.id.addDispBTN);
        deviceContainer = findViewById(R.id.linearLayout);
        emptyLabel = findViewById(R.id.emptyLabel);

        // Load all saved devices
        List<Device> devices = DeviceStorage.getDevices(this);
        if (devices.isEmpty()) { // If there are no devices, show an label for indication
            emptyLabel.setVisibility(View.VISIBLE);
        } else {
            emptyLabel.setVisibility(View.GONE);
        }


        LayoutInflater inflater = LayoutInflater.from(this);
        for (final Device d : devices) {
            View card = inflater.inflate(R.layout.device_card, deviceContainer, false);

            TextView tvTitle = card.findViewById(R.id.tvDeviceTitle);
            TextView tvDesc  = card.findViewById(R.id.tvDeviceDescription);

            tvTitle.setText(d.getTitle());
            tvDesc.setText(d.getDescription());

            // By clicking on the card, open the Box Activity
            card.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BoxPageActivity.class);
                intent.putExtra("device_title", d.getTitle());
                intent.putExtra("device_description", d.getDescription());
                intent.putExtra("device_id", d.getId());
                startActivity(intent);
            });

            // Edit/Details Page button
            ImageButton editButton = card.findViewById(R.id.iconEdit);
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DeviceDetails.class);
                intent.putExtra("device_title", d.getTitle());
                intent.putExtra("device_description", d.getDescription());
                intent.putExtra("device_id", d.getId());
                startActivity(intent);
            });

            // Remove device Page button
            ImageButton removeButton = card.findViewById(R.id.iconDelete);
            removeButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RemoveDevice.class);
                intent.putExtra("device_title", d.getTitle());
                intent.putExtra("device_description", d.getDescription());
                intent.putExtra("device_id", d.getId());
                startActivity(intent);
            });


            deviceContainer.addView(card);
        }

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
     * Generates a UUID for the user and uses it as MQTT clientID
     * @param context
     * @return
     */
    private String getOrCreateClientId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String clientId = prefs.getString("client_id", null);

        if (clientId == null) {
            clientId = UUID.randomUUID().toString();
            prefs.edit().putString("client_id", clientId).apply();
        }

        return clientId;
    }


}