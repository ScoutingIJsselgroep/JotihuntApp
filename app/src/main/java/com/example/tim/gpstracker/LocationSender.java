package com.example.tim.gpstracker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

public class LocationSender extends AsyncTask {
    private HttpsURLConnection urlConnection;
    private String payload;

    LocationSender(HttpURLConnection urlConnection, String payload) {
        this.urlConnection = (HttpsURLConnection) urlConnection;
        this.payload = payload;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d("GPSService", "Sending location data");
        try {
            TLSSocketFactory socketFactory = new TLSSocketFactory();
            urlConnection.setSSLSocketFactory(socketFactory);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

}