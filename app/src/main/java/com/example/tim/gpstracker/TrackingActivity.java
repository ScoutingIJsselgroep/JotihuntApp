package com.example.tim.gpstracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.location.LocationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


class LocationSender implements Runnable {
    HttpURLConnection urlConnection;
    String payload;

     LocationSender(HttpURLConnection urlConnection, String payload) {
         this.urlConnection = urlConnection;
         this.payload = payload;
    }

    @Override
    public void run() {
        System.out.println(payload);
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
            System.out.println(urlConnection.getResponseCode());
            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

public class TrackingActivity extends AppCompatActivity implements LocationListener {
    private EditText name;
    private Button toggle;
    private LocationManager locationManager;
    private String mprovider;
    URL url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        try {
            url = new URL("http://178.32.217.139:3000/api/car");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        name = (EditText) findViewById(R.id.editText2);
        toggle = (Button) findViewById(R.id.toggleButton2);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        mprovider = locationManager.getBestProvider(criteria, false);
        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locationManager.requestLocationUpdates(mprovider, 15000, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!(toggle.getText().equals("Aan"))) {
            System.out.println(toggle.getText());
            return;
        }
        if (name.getText().equals("")) {
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
