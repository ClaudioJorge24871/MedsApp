package com.example.medsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddNewDevice extends AppCompatActivity {

    private Button addDeviceButton;
    private EditText deviceTitle;
    private EditText deviceId;
    private EditText deviceDescription;


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
        // Go back arrow
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());
    }
}