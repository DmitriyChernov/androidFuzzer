package com.example.konyash.fuzzer.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.konyash.fuzzer.Control.Controller;
import com.example.konyash.fuzzer.fuzzer.AIDLFuzzer;
import com.example.konyash.fuzzer.fuzzer.Fuzzer;
import com.example.konyash.fuzzer.R;
import com.example.konyash.fuzzer.fuzzer.IFuzzer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class MainActivity extends Activity implements View.OnClickListener {

    // View variables.
    private TextView tvOut;
    private Button btnFuzz;
    private Button btnRead;
    private Button btnTest;
    private Button btnStop;
    private TextView textViewServiceName;

    private Controller controller;

    private boolean isRunning = false;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOut = (TextView) findViewById(R.id.tvOut);
        btnFuzz = (Button) findViewById(R.id.btnFuzz);
        btnTest = (Button) findViewById(R.id.btnTest);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnRead = (Button) findViewById(R.id.btnRead);
        textViewServiceName = (TextView) findViewById(R.id.textFieldServiceName);
        btnStop.setActivated(false);
        textViewServiceName.setActivated(false);

        btnFuzz.setOnClickListener(this);
        btnTest.setOnClickListener(this);

        try {
            controller = new Controller(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFuzz:
                controller.sendEmptyMessage(0);
                isRunning = true;
                break;
            case R.id.btnRead:
                controller.sendEmptyMessage(1);
                isRunning = true;
                break;
            case R.id.btnStop:
                controller.sendEmptyMessage(2);
                isRunning = false;
                break;
            case R.id.btnTest:
                // For debug //
                return;
        }

        btnStop.setActivated(isRunning);
        btnRead.setActivated(!isRunning);
        btnFuzz.setActivated(!isRunning);
    }

    public void setServiceNowFuzzing(String serviceName) {
        textViewServiceName.setText(serviceName);
    }
}