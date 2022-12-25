package com.philipp.currencyConverter;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private ExchangeRates rates = null;

    private Double roundDouble(int places, Double value) {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    private void updateSharedPrefsString(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void onclickConvert(View view) {
        Spinner spinner = findViewById(R.id.startSpinner);
        Spinner spinner2 = findViewById(R.id.destinationSpinner);
        EditText amount = findViewById(R.id.amountInput);
        TextView textView = findViewById(R.id.titleText);

        // Catch bad inputs
        if (amount.getText().toString().equals("")) {
            textView.setText("Empty input.");
            return;
        } else {
            Log.i("Info", "Input value: " + amount.getText().toString());
        }
        // Define text fields
        String text1 = amount.getText() + " " + spinner.getSelectedItem().toString();
        String text2 = ("\n≈ " + roundDouble(2, this.rates.convert(spinner.getSelectedItem().toString(), spinner2.getSelectedItem().toString(), Double.parseDouble(amount.getText().toString()))) + " " + spinner2.getSelectedItem().toString());

        // Apply styling
        SpannableString spannableString = new SpannableString(text1 + text2);
        spannableString.setSpan(new RelativeSizeSpan(0.5f), 0, text1.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply text
        textView.setText(spannableString);

        // Clear inputField
        amount.setText("");
    }

    public void onclickSwap(View view) {
        Spinner spinner = findViewById(R.id.startSpinner);
        Spinner spinner2 = findViewById(R.id.destinationSpinner);

        int spinnerPositionCache = spinner.getSelectedItemPosition();
        spinner.setSelection(spinner2.getSelectedItemPosition());
        spinner2.setSelection(spinnerPositionCache);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load currencies from cache
        this.rates = new ExchangeRates(this);

        // if today is not the date from the cached rates,
        // try to update the cache
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todaysDate = dateFormat.format(new Date());
        if (!todaysDate.equals(rates.getLastUpdated())) {
            rates.updateCache(this);
            rates = new ExchangeRates(this);
        }

        // update last updated text on the bottom
        TextView lastUpdated = findViewById(R.id.lastUpdated);
        if (todaysDate.equals(rates.getLastUpdated())) {
            lastUpdated.setText("last updated: today");
        } else {
            lastUpdated.setText("last updated: " + rates.getLastUpdated());
        }

        // Define spinners
        Spinner spinner = findViewById(R.id.startSpinner);
        Spinner spinner2 = findViewById(R.id.destinationSpinner);

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.rates.getCurrencyKeys());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Assign adapter
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        // get default values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String spinnerValue = prefs.getString("spinner1", "EUR");
        String spinnerValue2 = prefs.getString("spinner2", "USD");

        // Select default values
        for (int index = 0; index < spinner.getCount(); ++index) {
            if (spinner.getItemAtPosition(index).equals(spinnerValue)) {
                spinner.setSelection(index);
                break;
            }
        }
        for (int index = 0; index < spinner2.getCount(); ++index) {
            if (spinner2.getItemAtPosition(index).equals(spinnerValue2)) {
                spinner2.setSelection(index);
                break;
            }
        }

        EditText amount = findViewById(R.id.amountInput);

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

        // save selection in shared preferences
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateSharedPrefsString("spinner1", spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }

        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateSharedPrefsString("spinner2", spinner2.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }

        });

    }
}