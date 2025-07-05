package com.example.medsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class RemoveDevice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_remove_device);

        EditText titleView = findViewById(R.id.tituloCaixaText);
        EditText descView = findViewById(R.id.descCaixaText);
        TextView idView = findViewById(R.id.idenCaixaText);
        Button removeButton = findViewById(R.id.saveBTN);

        // Receive data from the Intent
        String title = getIntent().getStringExtra("device_title");
        String description = getIntent().getStringExtra("device_description");
        String id = getIntent().getStringExtra("device_id");

        // Set the texts fields with the received text
        titleView.setText(title);
        descView.setText(description);
        idView.setText(id);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idToRemove = idView.getText().toString();

                List<Device> devices = DeviceStorage.getDevices(RemoveDevice.this);
                Device deviceToRemove = null;

                // Search for the right device by id
                for (Device d : devices) {
                    if (d.getId().equals(idToRemove)) {
                        deviceToRemove = d;
                        break;
                    }
                }

                if (deviceToRemove != null) {
                    DeviceStorage.removeDevice(RemoveDevice.this, deviceToRemove);
                }

                // Back to home page
                Intent intent = new Intent(RemoveDevice.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}