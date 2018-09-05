package com.example.konyash.fuzzer.Control;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.konyash.fuzzer.fuzzer.AIDLFuzzer;
import com.example.konyash.fuzzer.fuzzer.Fuzzer;
import com.example.konyash.fuzzer.fuzzer.IFuzzer;
import com.example.konyash.fuzzer.main.MainActivity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by voidgib on 05.09.18.
 */

public class Controller extends Handler {

    // Name of services file in aidl annotation
    private final String aidlsFilename = "services";

    private final MainActivity mainActivity;
    private IFuzzer fuzzer;
    private Thread fuzzingThread;
    private static Context context;
    private static InputStream aidlsFile;

    public Controller(MainActivity mainActivity) throws IOException {
        this.mainActivity = mainActivity;
        context = mainActivity.getApplicationContext();
        aidlsFile = mainActivity.getResources().getAssets().open(aidlsFilename);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                fuzz();
                break;
            case 1:
                fuzzByFile();
            case 2:
                stop();
                break;
            case 3:
                setServiceNowFuzzing((String) msg.obj);
            default:
                break;
        }
    }

    public static Context getContext() {
        return context;
    }

    public static InputStream getAIDLFile() {
        return aidlsFile;
    }
    private void fuzz() {
        fuzzer = new Fuzzer(this);
        fuzzingThread = new Thread(fuzzer);
        fuzzingThread.start();
    }

    private void fuzzByFile() {
        fuzzer = new AIDLFuzzer(this);
        fuzzingThread = new Thread(fuzzer);
        fuzzingThread.start();
    }

    private void stop() {
        Log.d("Pigeon", "kurlyk");
        fuzzingThread.interrupt();
    }

    private void setServiceNowFuzzing(String serviceName) {
       mainActivity.setServiceNowFuzzing(serviceName);
    }
}
