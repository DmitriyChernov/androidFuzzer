package com.example.konyash.fuzzer.utils;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

public class DebugOutput {

    /**
     * Debug output. Prints declared methods for a target object class.
     * @param target debugging target object
     */
    public static void printDeclaredMethods(Class target)
    {
        Method[] meths = target.getDeclaredMethods();

        Log.i("declared methods for", target.getClass().getName());
        for (int i = 0; i < meths.length; i++){
            Log.i("method " + i + ":", meths[i].getName());
            Class<?>[] argt = meths[i].getParameterTypes();
            for (int j = 0; j < argt.length; j++) {
                Log.e("\targ" + j, " = " + argt[j].getName());
            }
        }
    }

    /**
     * Debug output. Prints declared fields for a target object class.
     * @param target debugging target object
     */
    public static void printDeclaredFields(Class target) {
        Field[] fields = target.getDeclaredFields();

        Log.i("declared fields for", target.getClass().getName());
        for (int i = 0; i < fields.length; i++) {
            Log.i("field " + i + ":", "type: " + fields[i].getType()
                    + "; name: " + fields[i].getName()
                    + ";\nmods: " + Modifier.toString(fields[i].getModifiers()) + "\n=====\n");
        }
    }

    /**
     * Debug output. Prints fields for a target object.
     * @param target debugging target object
     */
    public static void printFields(Object target) {
        Field[] fields = target.getClass().getFields();

        Log.i("declared fields for", target.getClass().getName());
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Object val = field.get(target);
                Log.i("field val", val.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Log.i("field " + i + ":", "type: " + fields[i].getType()
                    + "; name: " + fields[i].getName()
                    + ";\nmods: " + Modifier.toString(fields[i].getModifiers()) + "\n=====\n");
        }
    }

    /**
     * Debug output. Prints args inputed as object array for a target object class.
     * @param argv debugging objects array
     */
    public static void printArgs(Object[] argv)
    {
        Log.i("ARGS: ", "count = " + argv.length);
        for (int i = 0; i < argv.length; i++){
            Log.i("arg " + i + ":", "type = " + argv[i].getClass() + "; value: " + argv[i].toString() + " \n");
        }
    }
}
