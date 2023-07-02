package com.example.myapplication;

import static java.sql.DriverManager.println;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TotalValueActivity extends AppCompatActivity {

    private TextView totalValueTextView;
    private Button detectAnotherImageButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_value);

        totalValueTextView = findViewById(R.id.total_value_text_view);
        detectAnotherImageButton = findViewById(R.id.detect_another_image_button);

        // Obtener el valor total de las monedas desde el intent
        Intent intent = getIntent();
        double totalValue = intent.getDoubleExtra("totalValue", 0.0);

        // Mostrar el valor total de las monedas en el TextView
        totalValueTextView.setText("Valor total de las monedas: " + totalValue + "€");

        detectAnotherImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Volver a la pantalla principal
               // Intent intent = new Intent(TotalValueActivity.this, MainActivity.class);
               // startActivity(intent);
                println("hasta aqui llego");
            }
        });
    }
}
