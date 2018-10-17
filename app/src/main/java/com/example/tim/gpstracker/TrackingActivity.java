package com.example.tim.gpstracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

public class TrackingActivity extends AppCompatActivity {
    private EditText name;
    private Button toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        Log.d("GPSService", "Starting GPS service");
        startService(new Intent(this, GPSService.class));

        name = (EditText) findViewById(R.id.editText2);

        toggle = (Button) findViewById(R.id.toggleButton2);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
    }


}
