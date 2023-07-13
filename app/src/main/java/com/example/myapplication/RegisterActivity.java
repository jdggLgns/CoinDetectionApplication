package com.example.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class RegisterActivity extends AppCompatActivity {

    private String ipServices;

    private EditText nameEditText;
    private EditText mailEditText;
    private EditText useridEditText;
    private EditText passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Properties properties = ProjectProperties.getProperties();
        ipServices = properties.getProperty("ip_services");

        nameEditText = findViewById(R.id.name_edittext);
        mailEditText = findViewById(R.id.mail_edittext);
        useridEditText = findViewById(R.id.userid_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String mail = mailEditText.getText().toString();
                String userid = useridEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (userid.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor complete el usuario y/o contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(name, mail, userid, password);
                }
            }
        });
    }

    private void registerUser(String name, String mail, String userid, String password) {
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
                URL url = new URL("http://" + ipServices + "/register");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create JSON object with the parameters
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("name", name);
                jsonParams.put("mail", mail);
                jsonParams.put("userid", userid);
                jsonParams.put("password", password);

                // Write the JSON parameters to the request body
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonParams.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();

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
                Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
