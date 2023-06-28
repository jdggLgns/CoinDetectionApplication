package com.example.myapplication;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final String SERVER_URL = "http://52.212.181.135/send_image";
    private Button cameraButton;
    private ImageView homeButton;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = findViewById(R.id.camera_button);
        homeButton = findViewById(R.id.home);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                } else {
                    openCamera();
                }
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText usernameEditText = findViewById(R.id.username_edittext);
                EditText mailEditText = findViewById(R.id.mail_edittext);
                EditText useridEditText = findViewById(R.id.userid_edittext);
                EditText passwordEditText = findViewById(R.id.password_edittext);
                String name = usernameEditText.getText().toString();
                String mail = mailEditText.getText().toString();
                String userid = useridEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                insertDataIntoDatabase(name, mail, userid, password);
            }
        });

        // Check if registration was successful
        Intent intent = getIntent();
        boolean registrationSuccess = intent.getBooleanExtra("registration_success", false);
        if (registrationSuccess) {
            Toast.makeText(this, "Registro exitoso. Inicie sesión.", Toast.LENGTH_SHORT).show();
        }
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
                showTotalValueAlert(totalValue);
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

            // Envía la imagen al servidor en el cuerpo de la solicitud
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
     *
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
     *
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


    private void insertDataIntoDatabase(String name, String mail, String userid, String password) {
        new RegisterAsyncTask().execute(name, mail, userid, password);
    }

    private class RegisterAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String name = params[0];
            String mail = params[1];
            String userid = params[2];
            String password = params[3];

            try {
                URL url = new URL("http://52.212.181.135/register");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Crear los parámetros a enviar en la solicitud
                String parameters = "name=" + URLEncoder.encode(name, "UTF-8") +
                        "&mail=" + URLEncoder.encode(mail, "UTF-8") +
                        "&userid=" + URLEncoder.encode(userid, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");

                // Escribir los parámetros en el cuerpo de la solicitud
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // Leer la respuesta del servidor
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();

                // Verificar la respuesta del servidor
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean success = jsonResponse.getBoolean("success");

                return success;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Opcional: finaliza la actividad actual
            } else {
                Toast.makeText(MainActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
