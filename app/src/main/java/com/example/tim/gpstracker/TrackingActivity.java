package com.example.tim.gpstracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.content.Intent;

public class TrackingActivity extends AppCompatActivity {
    private EditText name;
    private ToggleButton toggle;
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = new Intent(this, GPSService.class);

        setContentView(R.layout.activity_tracking);

        name = (EditText) findViewById(R.id.nameField);

        toggle = (ToggleButton) findViewById(R.id.onOffButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle.isChecked()) {
                    Log.d("GPSActivity", "Starting GPS service");
                    intent.putExtra("Name", name.getText().toString());
                    name.setEnabled(false);
                    startService(intent);
                }
                else {
                    Log.d("GPSActivity", "Stopping GPS service");
                    name.setEnabled(true);
                    stopService(intent);
                }
            }
        });
    }


}
