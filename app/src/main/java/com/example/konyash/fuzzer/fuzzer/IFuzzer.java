package com.example.konyash.fuzzer.fuzzer;

import com.example.konyash.fuzzer.Control.Controller;
import com.example.konyash.fuzzer.generator.Value_Generator;
import com.example.konyash.fuzzer.logging.UDP_Client;
import com.example.konyash.fuzzer.main.MainActivity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by voidgib on 30.08.18.
 */

public abstract class IFuzzer implements  Runnable {

    // How much every method would invoke
    private final int callCount = 30;

    protected final Controller controller;
    protected final UDP_Client udpClient;
    protected final Invoker invoker;
    protected static Value_Generator valGenerator;

    public IFuzzer(Controller controller) {
        this.controller = controller;
        udpClient = new UDP_Client();
        valGenerator = new Value_Generator(controller, this);
        invoker = new Invoker(udpClient, valGenerator, callCount);
    }

    /**
     * Setting response string of radamsa call from TCP_Client
     * @param resp radamsa repsonse
     */
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

    /**
     * Start testing
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IOException
     */
    public abstract void fuzz() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException;

    @Override
    public void run() {
        try {
            fuzz();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | IOException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
