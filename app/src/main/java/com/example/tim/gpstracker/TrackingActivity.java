package com.example.tim.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.content.Intent;

import org.w3c.dom.Text;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class TrackingActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private EditText name;
    private ToggleButton toggle;
    public Intent intent;
    public Context context;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracking);

        context = getApplicationContext();
        intent = new Intent(this, GPSService.class);

        name = (EditText) findViewById(R.id.nameField);

        toggle = (ToggleButton) findViewById(R.id.onOffButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle.isChecked()) {
                    intent.setAction("StartService");
                    intent.putExtra("Name", name.getText().toString());
                    name.setEnabled(false);
                    context.startForegroundService(intent);
                }
                else {
                    intent.setAction("StopService");
                    name.setEnabled(true);
                    context.startForegroundService(intent);
                }
            }
        });

        String[] foregroundPerms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        if (!EasyPermissions.hasPermissions(this, foregroundPerms)) {
            EasyPermissions.requestPermissions(this, "De app heeft toegang nodig tot je locatie om je locatie door te kunnen sturen.", 123, foregroundPerms);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            String[] backgroundPerms = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
            if (!EasyPermissions.hasPermissions(this, backgroundPerms)) {
                EasyPermissions.requestPermissions(this, "De app heeft altijd toegang nodig om ook te kunnen blijven sturen als je scherm uit staat. Selecteer \"Altijd toestaan\" op het volgende scherm", 124, backgroundPerms);
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Kill foreground service
        intent.setAction("StopService");
        name.setEnabled(true);
        context.startForegroundService(intent);
    }
}
