package com.example.konyash.fuzzer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TCP_Client extends AsyncTask<String,Void,String> {
    private static final int TCP_SERVER_PORT = 4445;
    private Socket s;
    private com.example.konyash.fuzzer.MainActivity activity;

    public TCP_Client(com.example.konyash.fuzzer.MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(String res)
    {
        this.activity.setResp(res);
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onPostExecute(res);
    }

    @Override
    protected String doInBackground(String... params)
    {
        String res = "";
        try {
            // adb connect
            s = new Socket("192.168.100.4", TCP_SERVER_PORT);

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
            activity.setResp(res);
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