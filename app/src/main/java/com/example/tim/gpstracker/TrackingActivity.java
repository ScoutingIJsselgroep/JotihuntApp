package com.example.tim.gpstracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.location.LocationManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class LocationSender implements Runnable {
    private HttpURLConnection urlConnection;
    private String payload;

     LocationSender(HttpURLConnection urlConnection, String payload) {
         this.urlConnection = urlConnection;
         this.payload = payload;
    }

    @Override
    public void run() {
        Log.d("Send", "Trying to second location data");
        try {
            urlConnection.setReadTimeout(2500);
            urlConnection.setConnectTimeout(2500);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            OutputStream os = urlConnection.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            Log.d("Send", "Got response code " + String.valueOf(urlConnection.getResponseCode()));
            urlConnection.disconnect();

        } catch (IOException e) {
            Log.d("Send", "Failed to send location data");
            e.printStackTrace();
        }

    }
}

public class TrackingActivity extends AppCompatActivity implements LocationListener {
    private EditText name;
    private Button toggle;
    URL url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        Context context = getApplicationContext();
        PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();
        Log.d("WAKELOCK", "Wakelock acquired");

        try {
            url = new URL("https://www.eej.moe/api/car");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        name = (EditText) findViewById(R.id.editText2);
        toggle = (Button) findViewById(R.id.toggleButton2);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        String mprovider = locationManager.getBestProvider(criteria, false);
        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(mprovider, 2500, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("GPS", "Got new location data");
        if (!(toggle.getText().equals("Aan"))) {
            Log.d("GPS", "Not sending: button is off");
            return;
        }
        if (name.getText().toString().equals("")) {
            Log.d("GPS", "Not sending: name is empty");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("latitude", location.getLatitude());
            jsonObject.accumulate("longitude", location.getLongitude());
            jsonObject.accumulate("name", name.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String payload = jsonObject.toString();
        Log.d("GPS", "Payload: " + payload);

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            LocationSender locationSender = new LocationSender(urlConnection, payload);
            new Thread(locationSender).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
