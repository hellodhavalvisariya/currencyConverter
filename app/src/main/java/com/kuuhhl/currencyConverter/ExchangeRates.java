package com.kuuhhl.currencyConverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class ExchangeRates {
    private final HashMap<String, Double> currencies = new HashMap<>();
    private String lastUpdated;

    public ExchangeRates(Context context) {
        // test if we already have an updated version
        // of exchange rates.
        // if we do, we use that instead of the default values.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        lastUpdated = prefs.getString("lastUpdated", "2022-12-25");
        String cachedContents = "";

        if (lastUpdated.equals("2022-12-25")) {
            // get text from cached json file
            try {
                InputStream is = context.getAssets().open("defaultExchangeRates.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                cachedContents = new String(buffer, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            cachedContents = prefs.getString("exchangeRates", "{\"date\":\"2022-12-25\",\"rates\":{}}");
        }

        // parse text as json
        try {
            JSONObject jsonObj = new JSONObject(cachedContents);

            // get the update-date
            this.lastUpdated = jsonObj.getString("date");

            // add all currencies to our hashmap
            JSONObject rates = jsonObj.getJSONObject("rates");
            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                this.currencies.put(key, rates.getDouble(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void updateCache(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://api.exchangerate.host/latest?base=USD";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("res", response);
                // update exchange rates in shared preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("exchangeRates", response);

                // get last updated from newContents json
                String lastUpdated = "";
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    lastUpdated = jsonObj.getString("date");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // update variable
                editor.putString("lastUpdated", lastUpdated);

                editor.apply();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        });
        queue.add(request);

    }

    private Double getDollarExchangeRate(String currencyKey) {
        return this.currencies.get(currencyKey);
    }

    public String getLastUpdated() {
        return this.lastUpdated;
    }

    public String[] getCurrencyKeys() {
        // create array out of currency keys
        String[] res = currencies.keySet().toArray(new String[0]);

        // sort it alphabetically
        Arrays.sort(res);

        return res;
    }

    public Double convert(String startCurrencyKey, String goalCurrencyKey, Double amount) {
        // origin -> dollars -> goal
        Double inDollars = amount / this.getDollarExchangeRate(startCurrencyKey);
        return inDollars * this.getDollarExchangeRate(goalCurrencyKey);
    }

}
