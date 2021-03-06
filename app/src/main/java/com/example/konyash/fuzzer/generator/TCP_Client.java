package com.example.konyash.fuzzer.generator;

import android.os.AsyncTask;
import android.util.Log;

import com.example.konyash.fuzzer.fuzzer.Fuzzer;
import com.example.konyash.fuzzer.fuzzer.IFuzzer;
import com.example.konyash.fuzzer.main.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TCP_Client extends AsyncTask<String,Void,String> {
    private static final int TCP_SERVER_PORT = 4445;
    private static final String TCP_SERVER_IP = "192.168.0.12";
    private Socket s;
    private final IFuzzer fuzzer;

    public TCP_Client(IFuzzer fuzzer) {
        this.fuzzer = fuzzer;
    }

    /**
     * Work must to be done after tread was executed
     * @param res adds this value to response queue
     */
    @Override
    protected void onPostExecute(String res)
    {
        fuzzer.setRadamsaResponse(res);
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onPostExecute(res);
    }

    /**
     * Async task that communicates with remote radamsa
     * @param params must be an array of 2 Strings: first is a radamsa command; second is a options of call
     * @return results of radamsa call on remotes server
     */
    @Override
    protected String doInBackground(String... params)
    {
        String res = "";
        try {
            // adb connect
            s = new Socket(TCP_SERVER_IP, TCP_SERVER_PORT);

            // init
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            String outMsg = params[1];
            String options = params[0];

            // send output msg
            out.write(options);
            out.flush();

            // acknowledge
            String success = "";
            do {
                try {
                    success = in.readLine();
                } catch (Exception ex) {
                    success = "";
                    continue;
                }
            } while (!success.equals("ok"));

            out.flush();
            out.write(outMsg);
            out.flush();

            // accept server response
            res = in.readLine() + System.getProperty("line.separator");
            s.close();
            fuzzer.setRadamsaResponse(res);
        } catch (IOException e) {
            Log.e("TcpClient", "io crash");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("TcpClient", "some crash");
            e.printStackTrace();
        }
        return res;
    }
}