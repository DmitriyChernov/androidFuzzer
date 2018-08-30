package com.example.konyash.fuzzer.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private Button btnTest;
    private Toast toast;

    private IFuzzer fuzzer;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOut = (TextView) findViewById(R.id.tvOut);
        btnFuzz = (Button) findViewById(R.id.btnFuzz);
        btnTest = (Button) findViewById(R.id.btnTest);

        btnFuzz.setOnClickListener(this);
        btnTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFuzz:
                fuzzer = new Fuzzer(this);
                break;
            case R.id.btnRead:
                fuzzer = new AIDLFuzzer(this);
                break;
            case R.id.btnTest:
                // For debug //
                return;
        }

        try {
            fuzzer.fuzz();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}