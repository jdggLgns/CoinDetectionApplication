package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CamaraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final String SERVER_URL = "http://https://e46f-2a02-2e02-404-9400-2242-8442-3a80-6e82.ngrok-free.app/detect_coins"; // Reemplaza <tu_direccion_ip> por la dirección IP donde se ejecuta el servicio detect_coins
    private Button cameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);

        FrameLayout cameraContainer = findViewById(R.id.imagen_view_camara);
        cameraButton = cameraContainer.findViewById(R.id.camera_button);

        if (ContextCompat.checkSelfPermission(CamaraActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CamaraActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        cameraButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(CamaraActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CamaraActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            try {
                JSONObject coins = detectCoins(photo);
                if (coins != null) {
                    boolean success = coins.getBoolean("success");
                    if (success) {
                        calculateTotalValue(coins);

                    } else {
                        Toast.makeText(this, "Error en la detección de monedas", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error en la detección de monedas", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private double calculateTotalValue(JSONObject coins) throws JSONException {
        JSONObject resultados = coins.getJSONObject("resultado");
        double totalValue = 0.0;

        if (resultados.has("2euro")) {
            int apariciones2euro = resultados.getInt("2euro");
            totalValue += 2.0 * apariciones2euro;
        }
        if (resultados.has("1euro")) {
            int apariciones1euro = resultados.getInt("1euro");
            totalValue += 1.0 * apariciones1euro;
        }
        if (resultados.has("50cents")) {
            int apariciones50cents = resultados.getInt("50cents");
            totalValue += 0.5 * apariciones50cents;
        }
        if (resultados.has("20cents")) {
            int apariciones20cents = resultados.getInt("20cents");
            totalValue += 0.2 * apariciones20cents;
        }
        if (resultados.has("10cents")) {
            int apariciones10cents = resultados.getInt("10cents");
            totalValue += 0.1 * apariciones10cents;
        }
        if (resultados.has("5cents")) {
            int apariciones5cents = resultados.getInt("5cents");
            totalValue += 0.05 * apariciones5cents;
        }

        return totalValue;
    }


        private JSONObject detectCoins(Bitmap photo) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);

        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(encodedImage.getBytes());
            outputStream.flush();
            outputStream.close();

            InputStream in = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = in.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            String responseString = output.toString();
            return new JSONObject(responseString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
