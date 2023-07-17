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
import java.util.Properties;

public class AniadirProductoActivity extends AppCompatActivity {

    private EditText productNameEditText;
    private EditText priceEditText;
    private Button acceptButton;

    private SharedPreferences sharedPreferences;

    private boolean isEdit;
    private int productId;
    private String originalProductName;
    private String originalProductPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aniadir_transporte);

        productNameEditText = findViewById(R.id.product_name_edittext);
        priceEditText = findViewById(R.id.price_edittext);
        acceptButton = findViewById(R.id.accept_button);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);
        productId = intent.getIntExtra("productId", -1);
        originalProductName = intent.getStringExtra("productName");
        originalProductPrice = intent.getStringExtra("productPrice");

        if (isEdit && productId != -1 && originalProductName != null && originalProductPrice != null) {
            productNameEditText.setText(originalProductName);
            priceEditText.setText(originalProductPrice);
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName = productNameEditText.getText().toString();
                String price = priceEditText.getText().toString();
                String userId = sharedPreferences.getString("userId", "");

                if (isEdit) {
                    UpdateProductTask updateProductTask = new UpdateProductTask();
                    updateProductTask.execute(String.valueOf(productId), productName, price);
                } else {
                    CreateProductTask createProductTask = new CreateProductTask();
                    createProductTask.execute(userId, productName, price);
                }
            }
        });
    }

    private class CreateProductTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String userId = params[0];
            String productName = params[1];
            String price = params[2];

            ProjectProperties projectProperties = new ProjectProperties();
            Properties properties = projectProperties.getProperties();
            String ip = properties.getProperty("ip_services");

            String url = "http://" + ip + "/products";

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String requestBody = "{\"userid\":\"" + userId + "\", \"descripcion\":\"" + productName + "\", \"precio\":\"" + price + "\", \"tipo\":\"Transporte\"}";
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
                Toast.makeText(AniadirProductoActivity.this, "Producto creado exitosamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AniadirProductoActivity.this, TransporteActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(AniadirProductoActivity.this, "Error al crear el producto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateProductTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String productId = params[0];
            String productName = params[1];
            String price = params[2];

            ProjectProperties projectProperties = new ProjectProperties();
            Properties properties = projectProperties.getProperties();
            String ip = properties.getProperty("ip_services");

            String url = "http://" + ip + "/products/" + productId;

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String requestBody = "{\"descripcion\":\"" + productName + "\", \"precio\":\"" + price + "\"}";
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
                Toast.makeText(AniadirProductoActivity.this, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AniadirProductoActivity.this, TransporteActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(AniadirProductoActivity.this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
