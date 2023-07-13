package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView transportButton;
    private ImageView alimentosButton;
    private Button insertarAManoButton;
    private ImageView logoButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transportButton = findViewById(R.id.transporte_rescalado);
        alimentosButton = findViewById(R.id.alimentos);
        insertarAManoButton = findViewById(R.id.button_insertar_mano);
        logoButton = findViewById(R.id.logo_imageview);

        transportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransporteActivity.class);
                startActivity(intent);
            }
        });

       /* alimentosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlimentosActivity.class);
                startActivity(intent);
            }
        });

        insertarAManoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InsertarAManoActivity.class);
                startActivity(intent);
            }
        });
*/
    }
}
