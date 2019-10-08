package com.example.tim.gpstracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.UUID;

public class GPSService extends Service {
    private static final int LOCATION_INTERVAL = 2500;
    private static final float LOCATION_DISTANCE = 0;

    private LocationManager mLocationManager = null;

    PowerManager pm;
    PowerManager.WakeLock wl;

    String randomDefaultName = UUID.randomUUID().toString();

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER, randomDefaultName),
            new LocationListener(LocationManager.NETWORK_PROVIDER, randomDefaultName)
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("GPSService", "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            String argName = (String) extras.get("Name");
            if (!argName.isEmpty()) {
                for (LocationListener ll : mLocationListeners) {
                    ll.setName(argName);
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GPS Wakelock");
        wl.acquire();
        Log.d("GPSService", "WakeLock acquired!");

        initializeLocationManager();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("GPSService", "Insufficient permissions. Can't request location updates");
            return;
        }
        Log.d("GPSService", "Permissions ok. Requesting updates");

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
            Log.d("GPSService", "Requesting updates for GPS");
        } catch (java.lang.SecurityException ex) {
            Log.e("GPSService", "Failed to request location update. Ignoring", ex);
        } catch (IllegalArgumentException ex) {
            Log.e("GPSService", "GPS provider does not exist");
        }

       try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
            Log.d("GPSService", "Requesting updates for Network");
        } catch (java.lang.SecurityException ex) {
            Log.e("GPSService", "Failed to request location update. Ignoring", ex);
        } catch (IllegalArgumentException ex) {
            Log.e("GPSService", "Network provider does not exist");
        }
    }

    @Override
    public void onDestroy() {
        wl.release();
        Log.d("GPSService", "WakeLock released!");

        if (mLocationManager != null) {
            for (LocationListener listener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(listener);
                } catch (Exception ex) {
                    Log.e("GPSService", "Failed to remove location listeners. Ignoring", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
