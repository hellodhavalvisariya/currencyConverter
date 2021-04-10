package com.example.currency;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    public JsonObject getApiJson(String urlInput) {
        // Connect to the URL using java's native library
        JsonObject jObject = new JsonObject();
        try {
            URL url = new URL(urlInput);
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            jObject = new Gson().fromJson(new InputStreamReader((InputStream) request.getContent()), JsonObject.class); //Convert the input stream to a json element
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jObject;
    }

    public String[] getCurrencies() {
        JsonObject obj = getApiJson("https://openexchangerates.org/api/currencies.json");
        JsonArray jsonArray = obj.getAsJsonArray(); // TODO: Fix this bug. (Is not JSON array)
        int length = jsonArray.size();
        String[] currencies = new String[length];

        for (int index = 0; index < length; index++) {
            String key = jsonArray.get(index).getAsString();
            currencies[index] = key;
        }
        return currencies;
    }

    public float convertCurrency(float value, String from, String to) {
        // 1000 requests per month
        String authKey = "28f3a6b2a19646a0a0a737842c3fa0e2";

        JsonObject obj = getApiJson("https://openexchangerates.org/api/convert/" + value + "/" + from + "/" + to + "?app_id=" + authKey);
        float result = obj.get("response").getAsFloat();
        return result;
    }


    public void showResult(View view) {
        EditText inputField = (EditText) findViewById(R.id.amountInput);
        Spinner fromSpinner = (Spinner) findViewById(R.id.startSpinner);
        Spinner toSpinner = (Spinner) findViewById(R.id.destinationSpinner);
        TextView result = (TextView) findViewById(R.id.result);

        // change result Textbox to the converted value
        result.setText(Float.toString(convertCurrency(Float.valueOf(inputField.getText().toString()), fromSpinner.getSelectedItem().toString(), toSpinner.getSelectedItem().toString())));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check for internet connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Toast.makeText(this, "No internet connection found!", Toast.LENGTH_LONG).show();
        }

        // get currencies from API
        String[] currencies = getCurrencies();

        // load spinners
        Spinner fromSpinner = (Spinner) findViewById(R.id.startSpinner);
        Spinner toSpinner = (Spinner) findViewById(R.id.destinationSpinner);

        // TODO Properly implement the Spinners
        fromSpinner.setOnItemSelectedListener(this);
        toSpinner.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Setting the ArrayAdapter data on the Spinner
        fromSpinner.setAdapter(aa);
        toSpinner.setAdapter(aa);
    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(), currencies[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }


}

}