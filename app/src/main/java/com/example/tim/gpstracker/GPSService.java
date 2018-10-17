package com.example.tim.gpstracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GPSService extends Service {
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 2500;
    private static final float LOCATION_DISTANCE = 0;

    PowerManager pm;
    PowerManager.WakeLock wl;

    private class LocationSender extends AsyncTask {
        private HttpURLConnection urlConnection;
        private String payload;

        LocationSender(HttpURLConnection urlConnection, String payload) {
            this.urlConnection = urlConnection;
            this.payload = payload;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Log.d("GPSService", "Trying to send location data");
            try {
                urlConnection.setReadTimeout(2500);
                urlConnection.setConnectTimeout(2500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);

                try {
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(payload.getBytes());
                    os.flush();
                    Log.d("GPSService", "Got response code " + String.valueOf(urlConnection.getResponseCode()));
                    urlConnection.disconnect();
                } catch (Exception e) {
                    Log.d("GPSService", "Crashed at sending. Error: " + e);
                }

            } catch (IOException e) {
                Log.d("GPSService", "Failed to send location data");
                e.printStackTrace();
            }

            return null;
        }

    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        URL url;

        public LocationListener(String provider) {
            Log.e("GPSService", "LocationListener " + provider);
            mLastLocation = new Location(provider);

            try {
                url = new URL("http://www.eej.moe/api/car");
                Log.i("GPSService", "Building URL ok");
            } catch (MalformedURLException e) {
                Log.d("GPSService", "Building URL error: " + e.getMessage());
            }
        }
        @Override
        public void onLocationChanged(Location location) {
            Log.e("GPSService", "onLocationChanged: " + location);
            mLastLocation.set(location);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("latitude", mLastLocation.getLatitude());
                jsonObject.accumulate("longitude", mLastLocation.getLongitude());
                jsonObject.accumulate("name", "TestUser123");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String payload = jsonObject.toString();
            Log.d("GPSService", "Payload: " + payload);

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
            Log.e("GPSService", "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e("GPSService", "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e("GPSService", "onProviderDisabled: " + provider);
        }
    }

    GPSService.LocationListener[] mLocationListeners = new GPSService.LocationListener[] {
            new GPSService.LocationListener(LocationManager.GPS_PROVIDER),
            new GPSService.LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("GPSService", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GPS Wakelock");

        wl.acquire();

        Log.e("GPSService", "onCreate");
        initializeLocationManager();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("GPSService", "Insufficient permissions. Can't request location updates");
            return;
        }
        Log.i("GPSService", "Permissions ok. Requesting updates");

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i("GPSService", "Failed to request location update. Ignoring", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("GPSService", "GPS provider does not exist");
        }

        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i("GPSService", "Failed to request location update. Ignoring", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("GPSService", "Network provider does not exist");
        }
    }

    @Override
    public void onDestroy() {
        wl.release();

        Log.e("GPSService", "onDestroy");
        if (mLocationManager != null) {
            for (GPSService.LocationListener listener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(listener);
                } catch (Exception ex) {
                    Log.i("GPSService", "Failed to remove location listeners. Ignoring", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e("GPSService", "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
