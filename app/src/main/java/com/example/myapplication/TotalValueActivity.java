package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TotalValueActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_value);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        double precioAPagar = getIntent().getDoubleExtra("precio_a_pagar", 0.0);
        double precioDisponible = sharedPreferences.getFloat("precio_disponible", 0.0f);

        double mayorValor = Math.max(precioAPagar, precioDisponible);
        double menorValor = Math.min(precioAPagar, precioDisponible);
        double resta = mayorValor - menorValor;

        TextView textView = findViewById(R.id.total_value_textview);
        if (mayorValor == precioAPagar) {
            String mensaje = "Te faltan " + resta + " euros";
            textView.setText(mensaje);
        } else {
            String mensaje = "Te deben " + resta + " euros";
            textView.setText(mensaje);
        }
    }
}
