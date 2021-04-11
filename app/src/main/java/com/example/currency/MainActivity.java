package com.example.currency;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class MainActivity extends AppCompatActivity {
    public boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if it is first time using app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            //sync();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        } else {
            Toast.makeText(this, "Not first time.", Toast.LENGTH_LONG).show();
        }

    }
}
/*
private class GetCurrencies extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // show Progress bar and Toast
        ProgressBar pBar = findViewById(R.id.progressBar);
        pBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Syncing...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        HttpHandler sh = new HttpHandler();

        // Making a request to url and getting response
        String jsonStr = sh.makeServiceCall("https://openexchangerates.org/api/latest.json?app_id=db98850be67e4d3d9a3ac0cf26ea2e40");
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        // Replace JSON in preferences with the newer one
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("currencies", jsonStr);
        editor.apply();
    }
}
*/