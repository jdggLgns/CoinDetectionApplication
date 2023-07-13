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
    private static final String SERVER_URL = "http://35.245.125.86:5000/send_image";
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
                double totalValue = calculateTotalValue(coins);
                Intent intent = new Intent(this, TotalValueActivity.class);
                intent.putExtra("totalValue", totalValue);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private JSONObject detectCoins(Bitmap photo) throws JSONException {
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

    /**
     * Calcula el valor total de las monedas detectadas en la imagen, según la respuesta JSON del servidor.
     * @param coins objeto JSON que contiene la información sobre las monedas detectadas
     * @return valor total de las monedas detectadas
     * @throws JSONException si ocurre un error al leer el objeto JSON
     */
    private double calculateTotalValue(JSONObject coins) throws JSONException {
        double totalValue = 0.0;
        JSONArray coinsArray = coins.getJSONArray("coins");
        for (int i = 0; i < coinsArray.length(); i++) {
            JSONObject coin = coinsArray.getJSONObject(i);
            double value = coin.getDouble("value");
            totalValue += value;
        }
        return totalValue;
    }

    /**
     * Muestra una alerta con el valor total de las monedas detectadas en la imagen.
     * @param totalValue valor total de las monedas detectadas
     */
    private void showTotalValueAlert(double totalValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El valor total de las monedas detectadas es: " + totalValue + "€")
                .setTitle("Resultado")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Cerrar la alerta
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
