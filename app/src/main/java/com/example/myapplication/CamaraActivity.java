package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CamaraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final String SERVER_URL = "https://6a6a-2a02-2e02-404-9400-7dad-4619-379-f5aa.ngrok-free.app/detect_coins";
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
            new DetectCoinsTask().execute(photo);
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

    private class DetectCoinsTask extends AsyncTask<Bitmap, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Bitmap... bitmaps) {
            Bitmap photo = bitmaps[0];

            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data");

                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.png\"\r\n");
                writer.append("Content-Type: image/png\r\n\r\n");
                writer.flush();

                photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();

                writer.append("\r\n");
                writer.append("--").append(boundary).append("--\r\n");
                writer.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = connection.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    while ((bytesRead = in.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    String responseString = output.toString();
                    return new JSONObject(responseString);
                } else {
                    Log.e("Detect Coins", "Error en la llamada al servicio. Código de respuesta: " + responseCode);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(JSONObject coins) {
            if (coins != null) {
                try {
                    boolean success = coins.getBoolean("success");
                    if (success) {
                        double totalValue = calculateTotalValue(coins);
                        Toast.makeText(CamaraActivity.this, "Total Value: " + totalValue, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CamaraActivity.this, "Error1 en la detección de monedas", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(CamaraActivity.this, "Error2 en la detección de monedas", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
