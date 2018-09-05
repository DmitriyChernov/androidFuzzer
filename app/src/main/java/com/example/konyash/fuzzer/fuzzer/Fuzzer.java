package com.example.konyash.fuzzer.fuzzer;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;

import com.example.konyash.fuzzer.Control.Controller;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by voidgib on 30.08.18.
 */

public class Fuzzer extends IFuzzer {

    public Fuzzer(Controller controller) {
        super(controller);
    }

    /**
     * Start to fuzz all services that was got from context(MainActivity is a Context)
     */
    @Override
    public void fuzz() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ArrayList<String> ssNames = getSystemServicesFromContext();
        for (String ssName : ssNames) {
            Object sysService = Controller.getContext().getSystemService(ssName);
            try {
                udpClient.Message = "\n|||||||||||||||||||||\n"
                        + "Fuzzing started for "
                        + sysService.getClass().getName() + " class.\n";
                udpClient.sendMessage();
                Log.i("Object: ", "" + sysService);
                Message msg = new Message();
                msg.what = 3;
                msg.obj = sysService;
                controller.sendMessage(msg);
            } catch (NullPointerException ex) {
                continue;
            }

            try {
                Method[] meths = sysService.getClass().getMethods();
                for (Method meth : meths) {
                    if (isMethodToSkip(meth.getName())) {
                        continue;
                    }

                    if (!meth.isAccessible()) {
                        meth.setAccessible(true);
                    }


                    Class<?>[] typeParameters = meth.getParameterTypes();

                    String msg = "args_num = " + typeParameters.length + ": ";
                    ArrayList<String> tNames = new ArrayList<>();
                    for (Class<?> t : typeParameters) {
                        tNames.add(t.getName());
                        msg += t.getName()  + ", ";
                    }
                    msg += "!";
                    Log.i("ARGS:", msg);
                    invoker.invokeMethodByTypes(sysService, meth.getName(), tNames);
                }
            } catch (NullPointerException ex) {
            }
        }
    }



    /**
     * Getting all system service names
     * @return list of strings that is system service names
     */
    public ArrayList<String> getSystemServicesFromContext() {
        ArrayList<String> services = new ArrayList<>();
        Context context = Controller.getContext();
        Class contextClass = context.getClass();
        Field[] fields = contextClass.getFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            try {
                String type = fields[i].getType().getName();

                if (type.equals("java.lang.String")) {
                    Object val = field.get(context);
                    services.add(val.toString());
                    Log.i("sys service", val.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return services;
    }

}
