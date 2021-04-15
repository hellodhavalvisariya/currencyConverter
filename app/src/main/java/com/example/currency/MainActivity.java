package com.example.currency;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;


public class MainActivity extends AppCompatActivity {
    private HashMap<String, Double> currencies;

    public HashMap<String, Double> getCurrencies() {
        return this.currencies;
    }

    public void setCurrencies(HashMap<String, Double> value) {
        this.currencies = value;
    }

    public Double roundDouble(int places, Double value) {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public String[] sortArrayAlpha(String[] array) {
        Arrays.sort(array);
        return array;
    }

    public Double convertCurrencies(String originCurrency, String goalCurrency, Double value) {
        // origin -> base -> goal
        Double baseValue = value / getCurrencies().get(originCurrency);
        Double destinationValue = baseValue * getCurrencies().get(goalCurrency);
        return destinationValue;
    }

    public void onclickConvert(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.startSpinner);
        Spinner spinner2 = (Spinner) findViewById(R.id.destinationSpinner);
        EditText amount = (EditText) findViewById(R.id.amountInput);
        TextView textView = (TextView) findViewById(R.id.titleText);

        // Catch bad inputs
        if (amount.getText().toString().equals("")) {
            textView.setText("Empty input.");
            return;
        } else {
            Log.i("Info", "Input value: " + amount.getText().toString());
        }
        // Define text fields
        String text1 = amount.getText() + " " + spinner.getSelectedItem().toString();
        String text2 = ("\n≈ " + roundDouble(2, convertCurrencies(spinner.getSelectedItem().toString(), spinner2.getSelectedItem().toString(), Double.parseDouble(amount.getText().toString()))).toString() + " " + spinner2.getSelectedItem().toString());

        // Apply styling
        SpannableString spannableString = new SpannableString(text1 + text2);
        spannableString.setSpan(new RelativeSizeSpan(0.5f), 0, text1.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply text
        textView.setText(spannableString);

        // Clear inputField
        amount.setText("");
    }

    public void onclickSwap(View view) {
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
        HashMap<String, Double> currencyMap = new HashMap<String, Double>();
        try {
            JSONObject jsonObj = new JSONObject(json);

            Iterator<String> iterator = jsonObj.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                currencyMap.put(key, jsonObj.getDouble(key));
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortArrayAlpha(getCurrencies().keySet().toArray(new String[0])));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Assign adapter
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        // Select default values
        for (int index = 0; index < spinner.getCount(); ++index) {
            if (spinner.getItemAtPosition(index).equals("EUR")) {
                spinner.setSelection(index);
                break;
            }
        }
        for (int index = 0; index < spinner2.getCount(); ++index) {
            if (spinner2.getItemAtPosition(index).equals("USD")) {
                spinner2.setSelection(index);
                break;
            }
        }

        EditText amount = (EditText) findViewById(R.id.amountInput);

        amount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onclickConvert(null);
                    return true;
                }
                return false;
            }
        });

    }
}