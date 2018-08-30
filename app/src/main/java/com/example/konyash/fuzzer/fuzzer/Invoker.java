package com.example.konyash.fuzzer.fuzzer;

import android.util.Log;

import com.example.konyash.fuzzer.logging.UDP_Client;
import com.example.konyash.fuzzer.generator.Value_Generator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by voidgib on 30.08.18.
 */

public class Invoker {

    private final UDP_Client udpClient;
    private Value_Generator generator;
    private final int callCount;

    public Invoker(UDP_Client udpClient, Value_Generator generator, int callCount) {
        this.udpClient = udpClient;
        this.generator = generator;
        this.callCount = callCount;
    }

    public void invokeMethod(Object obj, String meth, Class[] argt, Object[] argv) {
        int argc = argt.length;
        Log.i("invoke: ", " obj: " + obj
                + "\n method: " + meth);

        Class c = obj.getClass();
        // TODO: save return value of a calling method.
        // Object result;

        try {
            Method invokingMeth = c.getMethod(meth, argt);
            invokingMeth.setAccessible(true);
            invokingMeth.invoke(obj, argv);
            String argsMsg = "";
            for (int i = 0; i < argc; i++) {
                argsMsg += argv[i] + ", ";
            }

            udpClient.Message = "Method: " + meth + ", with parameters:" + argsMsg
                    + " finished test succesfully!\n";
            udpClient.Message += "============\n";
            udpClient.sendMessage();
        } catch (InvocationTargetException ex) {
            String msg = "InvocationTargetException: class " + c.getName();
            Log.e("Invoke_ex", msg);
            msg += "============\n";
            udpClient.Message = msg;
            udpClient.sendMessage();
        } catch (NoSuchMethodException ex) {
            String msg = "NoSuchMethodException: class " + c.getName() + "No such method: " + meth;
            Log.e("Invoke_ex", msg);
            msg += "============\n";
            udpClient.Message = msg;
            udpClient.sendMessage();
        } catch (Exception e){
            String msg = "Exception when invoking \"" + meth + "\" method"
                    + "with args:";
            Log.e("Exception", msg);
            e.printStackTrace();

            msg += "\n";
            for (int k = 0; k < argv.length; k ++) {
                String valstr;
                if (argv[k] == null)
                    valstr = "null";
                else
                    valstr = argv[k].toString();

                String arg = k + " arg(" + argt[k].toString() + ") :" + valstr;
                Log.e("Exception", arg);

                msg += arg + "\n";
            }
            Log.e("Exception", e.toString());

            msg += e.toString();
            msg += "============\n";
            udpClient.Message = msg;
            udpClient.sendMessage();
        }

    }

    // Invokes target method of target object. It is system call in target context.
    public void invokeMethodByTypes(Object obj, String meth, ArrayList<String> types)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        int argc = types.size();

        for (int j = 0; j < callCount; j ++) {
            Object[] argv = new Object[argc];
            Class[] argt = new Class[argc];

            String argsMsg = argc + "(";
            for (int i = 0; i < argc; i ++ ) {
                try {
                    argv[i] = generator.generateValue(types.get(i));
                    argt[i] = generator.getType(types.get(i));
                    argsMsg += argt[i].getName();
                    if (i != argt.length - 1) {
                        argsMsg += ", ";
                    }

                    Log.i("argType:", argt[i] + ": " + argv[i]);
                } catch (NullPointerException ex) {
                    return;
                }
            }

            argsMsg += ")";

            try {
                String msg = "\n============\n";
                msg += j + ") method: " + meth + " on " +
                        j + " iteration; with arg types: " + argsMsg
                        + " started to test.\n";

                udpClient.Message = msg;
                Log.i("invoke", msg);
                udpClient.sendMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }

            invokeMethod(obj, meth, argt, argv);
        }
    }
}
