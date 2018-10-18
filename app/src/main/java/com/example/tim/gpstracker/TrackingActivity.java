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
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = new Intent(this, GPSService.class);

        setContentView(R.layout.activity_tracking);

        name = (EditText) findViewById(R.id.editText2);

        toggle = (Button) findViewById(R.id.toggleButton2);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle.getText().equals("Aan")) {
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
