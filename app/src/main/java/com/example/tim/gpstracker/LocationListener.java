package com.example.tim.gpstracker;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LocationListener implements android.location.LocationListener {
    private Location mLastLocation;
    private URL url;
    private String name;

    public LocationListener(String provider, String name) {
        this.name = name;

        Log.d("GPSService", "LocationListener " + provider);
        mLastLocation = new Location(provider);

        try {
            url = new URL("https://www.eej.moe/api/car");
        } catch (MalformedURLException e) {
            Log.d("GPSService", "Error building URL");
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("GPSService", "onLocationChanged: " + location);
        mLastLocation.set(location);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("latitude", mLastLocation.getLatitude());
            jsonObject.accumulate("longitude", mLastLocation.getLongitude());
            jsonObject.accumulate("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String payload = jsonObject.toString();
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            LocationSender locationSender = new LocationSender(urlConnection, payload);
            locationSender.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("GPSService", "onStatusChanged: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("GPSService", "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("GPSService", "onProviderDisabled: " + provider);
    }
}