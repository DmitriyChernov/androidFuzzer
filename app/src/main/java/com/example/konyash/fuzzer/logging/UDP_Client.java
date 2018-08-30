package com.example.konyash.fuzzer.logging;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_Client {
    private AsyncTask<Void, Void, Void> async_cient;
    public String Message;

    public synchronized void sendMessage() {
        async_cient = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String msg = Message;
                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    int port = 4444;
                    InetAddress IPAddress = InetAddress.getByName("10.0.2.2");

                    dp = new DatagramPacket(msg.getBytes()
                            , msg.length()
                            , IPAddress
                            , port);
                    //ds.setBroadcast(true);
                    ds.send(dp);

                    ds.close();
                } catch (Exception e) {
                    Log.e("ex", Message.length() + "| " + Message);
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        };

        try {
            Thread.currentThread().sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 11)
            async_cient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_cient.execute();

    }
}