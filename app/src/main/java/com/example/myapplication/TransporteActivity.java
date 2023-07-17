package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transporte);

        Properties properties = ProjectProperties.getProperties();
        ipServices = properties.getProperty("ip_services");

        transportButtonLayout = findViewById(R.id.transport_button_layout);
        addProductButton = findViewById(R.id.add_product_button);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransporteActivity.this, AniadirProductoActivity.class);
                intent.putExtra("isEdit", false);
                startActivity(intent);
            }
        });
        userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", "");
        getProductsByUserId();
    }

    private void createTransportButton(String productName, String productPrice, int productId) {
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
        transportButton.setBackgroundColor(getResources().getColor(android.R.color.system_neutral2_400));

        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                160, // Width
                160  // Height
        );

        ImageView productImageView = new ImageView(this);
        productImageView.setLayoutParams(imageLayoutParams);
        productImageView.setImageResource(R.drawable.home);

        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textLayoutParams.setMarginStart(20);

        LinearLayout productInfoLayout = new LinearLayout(this);
        productInfoLayout.setLayoutParams(textLayoutParams);
        productInfoLayout.setOrientation(LinearLayout.VERTICAL);
        productInfoLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView productNameTextView = new TextView(this);
        productNameTextView.setLayoutParams(textLayoutParams);
        productNameTextView.setText(productName);
        productNameTextView.setTextColor(getResources().getColor(android.R.color.white));
        productNameTextView.setTextSize(24);

        TextView productPriceTextView = new TextView(this);
        productPriceTextView.setLayoutParams(textLayoutParams);
        productPriceTextView.setText(productPrice);
        productPriceTextView.setTextColor(getResources().getColor(android.R.color.white));
        productPriceTextView.setTextSize(22);

        productInfoLayout.addView(productNameTextView);
        productInfoLayout.addView(productPriceTextView);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                80, // Width
                120  // Height
        );

        Button editButton = new Button(this);
        editButton.setLayoutParams(buttonLayoutParams);
        editButton.setText("M");
        editButton.setTextSize(14);
        editButton.setTextColor(getResources().getColor(android.R.color.white));
        editButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransporteActivity.this, AniadirProductoActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("productId", productId);
                intent.putExtra("productName", productName);
                intent.putExtra("productPrice", productPrice);
                startActivity(intent);
            }
        });
        transportButton.addView(editButton);

        Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(buttonLayoutParams);
        deleteButton.setText("X");
        deleteButton.setTextSize(14);
        deleteButton.setTextColor(getResources().getColor(android.R.color.white));
        deleteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct(productId);
            }
        });
        transportButton.addView(deleteButton);

        transportButton.addView(productImageView);
        transportButton.addView(productInfoLayout);

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
                URL url = new URL("http://" + ipServices + "/products/usuario/" + userId);
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
                        products = jsonResponse.getJSONArray("productos");
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
                        int productId = product.getInt("id");

                        createTransportButton(productName, productPrice, productId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(TransporteActivity.this, "Error al obtener los productos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteProduct(int productId) {
        new DeleteProductTask().execute(productId);
    }

    private class DeleteProductTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            int productId = params[0];

            try {
                URL url = new URL("http://" + ipServices + "/products/" + productId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(TransporteActivity.this, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show();
                transportButtonLayout.removeAllViews();
                getProductsByUserId();
            } else {
                Toast.makeText(TransporteActivity.this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
