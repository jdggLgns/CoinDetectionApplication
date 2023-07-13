package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AniadirProductoActivity extends AppCompatActivity {

    private EditText productNameEditText;
    private EditText priceEditText;
    private Button acceptButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aniadir_transporte);

        productNameEditText = findViewById(R.id.product_name_edittext);
        priceEditText = findViewById(R.id.price_edittext);
        acceptButton = findViewById(R.id.accept_button);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName = productNameEditText.getText().toString();
                String price = priceEditText.getText().toString();
                String userId = sharedPreferences.getString("userId", "");
                CreateProductTask createProductTask = new CreateProductTask();
                createProductTask.execute(userId, productName, price);
            }
        });
    }

    private class CreateProductTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String userId = params[0];
            String productName = params[1];
            String price = params[2];
            String url = "http://3.249.85.29:5003/products";

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String requestBody = "{\"userid\":\"" + userId + "\", \"descripcion\":\"" + productName + "\", \"precio\":\"" + price + "\", \"tipo\":\"\"}";
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(requestBody);
                wr.flush();
                wr.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent(AniadirProductoActivity.this, TransporteActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(AniadirProductoActivity.this, "Error al crear el producto", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

