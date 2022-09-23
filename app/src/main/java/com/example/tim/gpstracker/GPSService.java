package com.example.tim.gpstracker;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class GPSService extends Service {
    private static final int LOCATION_INTERVAL = 5000;
    private static final float LOCATION_DISTANCE = 0;

    private LocationManager mLocationManager = null;

    PowerManager pm;
    PowerManager.WakeLock wl;

    String randomDefaultName = UUID.randomUUID().toString();

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER, randomDefaultName),
            new LocationListener(LocationManager.NETWORK_PROVIDER, randomDefaultName)
    };

    private boolean locationPermissionsGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent.getAction().equals("StopService")) {
            stopForeground(true);
            stopSelfResult(startId);
            return START_NOT_STICKY;
        }
        else { //StartService
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
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String CHANNEL_ID = "jotihunt_01";
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Jotihunt GPS Tracker", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();

        startForeground(1337, notification);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gpstracker:GPS Wakelock");
        wl.acquire();
        Log.d("GPSService", "WakeLock acquired!");

        initializeLocationManager();

        if (!locationPermissionsGranted()) {
            Log.e("GPSService", "Insufficient permissions. Can't request location updates");
            Toast.makeText(getApplicationContext(), "GPS permissions niet in orde. Geef deze app handmatig rechten.", Toast.LENGTH_LONG).show();
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
