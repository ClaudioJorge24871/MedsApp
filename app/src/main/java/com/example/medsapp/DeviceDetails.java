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

public class DeviceDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_details);

        EditText titleView = findViewById(R.id.tituloCaixaText);
        EditText descView = findViewById(R.id.descCaixaText);
        TextView idView = findViewById(R.id.idenCaixaText);
        Button saveButton = findViewById(R.id.saveBTN);

        // Receive data from the Intent
        String title = getIntent().getStringExtra("device_title");
        String description = getIntent().getStringExtra("device_description");
        String id = getIntent().getStringExtra("device_id");

        titleView.setText(title);
        descView.setText(description);
        idView.setText(id);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTitle = titleView.getText().toString().trim();
                String newDesc = descView.getText().toString().trim();
                String newId = idView.getText().toString().trim();

                if (newTitle.isEmpty()) {
                    titleView.setError("Required");
                    return;
                }

                if (newId.isEmpty()) {
                    idView.setError("Required");
                    return;
                }

                // Save changes
                List<Device> devices = DeviceStorage.getDevices(DeviceDetails.this);
                for (int i = 0; i < devices.size(); i++) {
                    Device device = devices.get(i);
                    if (device.getId().equals(getIntent().getStringExtra("device_id"))) {
                        device.setTitle(newTitle);
                        device.setDescription(newDesc);
                        device.setId(newId);
                        break;
                    }
                }

                DeviceStorage.saveDevices(DeviceDetails.this, devices);

                Intent intent = new Intent(DeviceDetails.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}