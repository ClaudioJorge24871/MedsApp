package com.example.medsapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class BoxPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_box_page);

        TextView boxNameTitle = findViewById(R.id.boxNameView);
        EditText descText = findViewById(R.id.descricaoCaixaText);
        EditText identText = findViewById(R.id.identificadorCaixaText);

        String title = getIntent().getStringExtra("device_title");
        String id = getIntent().getStringExtra("device_id");
        String desc = getIntent().getStringExtra("device_description");

        boxNameTitle.setText(title);
        descText.setText(desc);
        identText.setText(id);

        // Go back arrow
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());
    }
}