package com.example.konyash.fuzzer.fuzzer;

import com.example.konyash.fuzzer.generator.Value_Generator;
import com.example.konyash.fuzzer.logging.UDP_Client;
import com.example.konyash.fuzzer.main.MainActivity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by voidgib on 30.08.18.
 */

public abstract class IFuzzer {


    // How much every method would invoke
    private final int callCount = 30;

    protected final MainActivity mainActivity;
    protected final UDP_Client udpClient;
    protected final Invoker invoker;
    protected static Value_Generator valGenerator;

    public IFuzzer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        udpClient = new UDP_Client();
        valGenerator = new Value_Generator(mainActivity);
        invoker = new Invoker(udpClient, valGenerator, callCount);
    }

    public void setRadamsaResponse(String resp) {
        valGenerator.setResponse(resp);
    }

    public static int getResponseQueueSize() {
        return valGenerator.responseQueueSize();
    }

    protected boolean isMethodToSkip(String meth) {
        if (meth.equals("reboot")
                || meth.equals("shutdown")
                || meth.equals("clearApplicationUserData")
                || meth.equals("factoryReset")
                || meth.equals("disconnect"))
            return true;
        return false;
    }

    public abstract void fuzz() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException;
}
