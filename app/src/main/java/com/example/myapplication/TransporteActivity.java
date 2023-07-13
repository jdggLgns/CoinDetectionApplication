package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class TransporteActivity extends AppCompatActivity {

    private LinearLayout transportButtonLayout;
    private Button addProductButton;

    private int buttonCounter = 0;

    private String ipServices;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transporte);

        Properties properties = ProjectProperties.getProperties();
        ipServices = properties.getProperty("ip_services");

        transportButtonLayout = findViewById(R.id.transport_button_layout);
        addProductButton = findViewById(R.id.add_product_button);

        /*addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTransportButton();
            }
        });*/

        userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", "");
        getProductsByUserId();
    }

    private void createTransportButton(String productName, String productPrice) {
        buttonCounter++;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 20, 0, 0);

        LinearLayout transportButton = new LinearLayout(this);
        transportButton.setId(View.generateViewId());
        transportButton.setLayoutParams(layoutParams);
        transportButton.setOrientation(LinearLayout.HORIZONTAL);
        transportButton.setPadding(10, 10, 10, 10);
        transportButton.setClickable(true);
        transportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TransporteActivity.this, "Transport Button " + transportButton.getId() + " clicked", Toast.LENGTH_SHORT).show();
                // Implementar acciones al hacer clic en el botón de transporte específico
            }
        });

        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        TextView productNameTextView = new TextView(this);
        productNameTextView.setLayoutParams(textLayoutParams);
        productNameTextView.setText(productName);
        productNameTextView.setTextColor(getResources().getColor(android.R.color.white));
        productNameTextView.setTextSize(18);

        TextView productPriceTextView = new TextView(this);
        productPriceTextView.setLayoutParams(textLayoutParams);
        productPriceTextView.setText(productPrice);
        productPriceTextView.setTextColor(getResources().getColor(android.R.color.white));
        productPriceTextView.setTextSize(16);

        transportButton.addView(productNameTextView);
        transportButton.addView(productPriceTextView);

        transportButtonLayout.addView(transportButton);
    }

    private void getProductsByUserId() {
        new GetProductsAsyncTask().execute();
    }

    private class GetProductsAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private JSONArray products;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("http://" + ipServices + "/get-products?userId=" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
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
                    if (success) {
                        products = jsonResponse.getJSONArray("products");
                        return true;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                for (int i = 0; i < products.length(); i++) {
                    try {
                        JSONObject product = products.getJSONObject(i);
                        String productName = product.getString("descripcion");
                        String productPrice = product.getString("precio");

                        createTransportButton(productName, productPrice);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(TransporteActivity.this, "Error al obtener los productos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
