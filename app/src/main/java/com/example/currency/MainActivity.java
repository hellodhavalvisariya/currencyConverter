package com.example.currency;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {
    private HashMap<String, Double> currencies;

    public HashMap<String, Double> getCurrencies() {
        return this.currencies;
    }

    public void setCurrencies(HashMap<String, Double> value) {
        this.currencies = value;
    }

    public Double convertCurrencies(String originCurrency, String goalCurrency, float value) {
        Double dollarValue = value * getCurrencies().get(originCurrency);
        Double destinationValue = dollarValue / getCurrencies().get(goalCurrency);
        return destinationValue;

    }

    public void onclickConvert(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.startSpinner);
        Spinner spinner2 = (Spinner) findViewById(R.id.destinationSpinner);
        EditText amount = (EditText) findViewById(R.id.amountInput);
        TextView textView = (TextView) findViewById(R.id.titleText);

        textView.setText(convertCurrencies(spinner.getSelectedItem().toString(), spinner2.getSelectedItem().toString(), Float.parseFloat(amount.getText().toString())).toString());

    }
    public void onclickSwap(View view){
        Spinner spinner = (Spinner) findViewById(R.id.startSpinner);
        Spinner spinner2 = (Spinner) findViewById(R.id.destinationSpinner);

        int spinnerPositionCache = spinner.getSelectedItemPosition();
        spinner.setSelection(spinner2.getSelectedItemPosition());
        spinner2.setSelection(spinnerPositionCache);
    }

    public HashMap<String, Double> loadCurrencies() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("exchangeRates.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        HashMap<String, Double> currencyMap = null;
        try {
            JSONObject jsonObj = new JSONObject(json);

            Iterator<String> iterator = jsonObj.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                currencyMap.put(key, jsonObj.getDouble(key)); // TODO: Fix bug: null object reference
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencyMap;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load currencies
        setCurrencies(loadCurrencies());

        // Define spinners
        Spinner spinner = (Spinner) findViewById(R.id.startSpinner);
        Spinner spinner2 = (Spinner) findViewById(R.id.destinationSpinner);

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getCurrencies().keySet().toArray(new String[0]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Assign adapter
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);

    }
}