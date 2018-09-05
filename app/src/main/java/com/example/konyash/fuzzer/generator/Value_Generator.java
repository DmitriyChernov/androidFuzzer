package com.example.konyash.fuzzer.generator;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Parcel;
import android.util.Log;

import com.example.konyash.fuzzer.Control.Controller;
import com.example.konyash.fuzzer.fuzzer.IFuzzer;
import com.example.konyash.fuzzer.utils.DebugOutput;
import com.example.konyash.fuzzer.utils.ReflectionUtils;
import com.example.konyash.fuzzer.main.MainActivity;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Value_Generator {
    private final Controller controller;
    private final IFuzzer fuzzer;

    private static Queue<String> responseQueue = new LinkedList<String>();

    // Array that stores classes that was parsed. Need for generate instance of java.lang.Class.
    ArrayList<Class> classStore;

    /**
     * Creates primitive and object values
     * @param controller handler of main activity to get context
     * @param fuzzer to set radamsa response
     */
    public Value_Generator(Controller controller, IFuzzer fuzzer)
    {
        this.controller = controller;
        this.fuzzer = fuzzer;
        classStore = new ArrayList<>();

        fillClassStore();
    }

    /**
     * Fills list of classes that was used in work to create objects of them further
     */
    private void fillClassStore()
    {
        ReflectionUtils reflections = new ReflectionUtils();

        try {
            List<String> classes = reflections.getAllClasses(Controller.getContext());
            //Log.e("classes len:", "" + classes.size());
            for (int i = 0; i < classes.size(); i++) {
                try {
                    Class instance = Class.forName(classes.get(i));
                    classStore.add(instance);
                } catch (ClassNotFoundException e) {
                    continue;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns class or type by that name
     * @param name name of class or type
     * @return type by his name. Needed to parse primitive types name.
     */
    public static Class getType(String name)
    {
        Class res;
        try {
            res = Class.forName(name);
            return res;
        } catch (ClassNotFoundException e){
            if (name.equals("byte") || name.equals("java.lang.Byte")) return byte.class;
            if (name.equals("short") || name.equals("java.lang.Short")) return short.class;
            if (name.equals("int") || name.equals("java.lang.Integer")) return int.class;
            if (name.equals("long") || name.equals("java.lang.Long")) return long.class;
            if (name.equals("char") || name.equals("java.lang.Character")) return char.class;
            if (name.equals("float") || name.equals("java.lang.Float")) return float.class;
            if (name.equals("double") || name.equals("java.lang.Double")) return double.class;
            if (name.equals("boolean") || name.equals("java.lang.Boolean")) return boolean.class;
            if (name.equals("void") || name.equals("java.lang.Void")) return void.class;
        }

        return Object.class;
    }

    /**
     * Generates value of specified name of type or class
     * @param type name of class or type
     * @return generated value
     */
    public Object generateValue(String type) {
        try {
            classStore.add(Class.forName(type));
        } catch (ClassNotFoundException e) {}

        switch (type){
            case "int":                     return generateInteger();
            case "java.lang.String":        return generateString();
            case "java.lang.CharSequence":  return generateString();
            case "boolean":                 return generateBoolean();
            case "long":                    return generateLong();
            case "float":                   return generateFloat();
            case "double":                  return generateDouble();
            case "char":                    return generateChar();
            case "byte":                    return generateByte();
            default:                        return prepareTestObjectByClass(type, new ArrayList<String>());
        }
    }

    /**
     * Generates value of specified name of type or class
     * @param type name of class or type
     * @param callStack stack of construction
     * @return generated value
     */
    public Object generateValue(String type, ArrayList<String> callStack) {
        try {
            classStore.add(Class.forName(type));
        } catch (ClassNotFoundException e) {}

        switch (type){
            case "int":                     return generateInteger();
            case "java.lang.String":        return generateString();
            case "java.lang.CharSequence":  return generateString();
            case "boolean":                 return generateBoolean();
            case "long":                    return generateLong();
            case "float":                   return generateFloat();
            case "double":                  return generateDouble();
            case "char":                    return generateChar();
            case "byte":                    return generateByte();
            default:                        return prepareTestObjectByClass(type, callStack);
        }
    }

    /**
     * Calls radamsa to mutate value
     * @param input radamsa intruction
     * @param options radamsa options
     * @return radamsa response: mutated value
     */
    private String radamsaCall(String input, String options)
    {
        while (true) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(new RadamsaCall(fuzzer, options, input));
            try {
                future.get(3, TimeUnit.SECONDS);
                return responseQueue.poll();
            } catch (Exception e) {
                future.cancel(true);
                continue;
            } finally {
                executor.shutdownNow();
            }
        }
    }

    /**
     * Addss radamsa response to response queue
     * @param resp radamsa response: mutated value
     */
    public static void setResponse(String resp) {
        responseQueue.add(resp);
    }

    /**
     *
     * @return size of radamsa response queue
     */
    public static int responseQueueSize() {
        return responseQueue.size();
    }

    /**
     * Generates Integer object. Cycle needs to pass mutated values that cannot be casted to target type
     * @return object that is int value
     */
    private Object generateInteger()
    {
        Random rand = new Random();
        String out;
        String in = String.valueOf(rand.nextInt());
        for (;;) {
            out = radamsaCall(in, "-m num=1");
            try {
                int res = Integer.parseInt(out.replace("\n", ""));
                return res;
            } catch (Exception ex) { continue; }
        }
    }

    /**
     * Generates Long object. Cycle needs to pass mutated values that cannot be casted to target type
     * @return object that is long value
     */
    private Object generateLong()
    {
        Random rand = new Random();
        String out;
        String in = String.valueOf(rand.nextLong());
        for (;;) {
            out = radamsaCall(in, "-m num=2");
            try {
                long res = Long.parseLong(out.replace("\n", ""));
                return res;
            } catch (Exception ex) { continue; }
        }
    }

    /**
     * Generates Float object. Cycle needs to pass mutated values that cannot be casted to target type
     * @return object that is float value
     */
    private Object generateFloat()
    {
        Random rand = new Random();
        String out;
        String in = String.valueOf(rand.nextFloat());
        for (;;) {
            out = radamsaCall(in, "-m num=1");
            try {
                float res = Float.parseFloat(out.replace("\n", ""));
                return res;
            } catch (Exception ex) { continue; }
        }
    }

    /**
     * Generates Double object. Cycle needs to pass mutated values that cannot be casted to target type
     * @return object that is double value
     */
    private Object generateDouble()
    {
        Random rand = new Random();
        String out;
        String in = String.valueOf(rand.nextDouble());
        for (;;) {
            out = radamsaCall(in, "-m num=1");
            try {
                double res = Double.parseDouble(out.replace("\n", ""));
                return res;
            } catch (Exception ex) { continue; }
        }
    }

    /**
     * Generates Byte object. Cycle needs to pass mutated values that cannot be casted to target type
     * @return object that is byte value
     */
    private Object generateByte()
    {
        Random rand = new Random();
        byte[] b = new byte[1];
        rand.nextBytes(b);

        return b[0];
    }

    /**
     * Generates Boolean object.
     * @return object that is float value
     */
    private Object generateBoolean()
    {
        Random rand = new Random();
        return rand.nextBoolean();
    }

    /**
     * Generates alphanumeric char object.
     * @return object that is char value
     */
    private Object generateChar()
    {
        String randStr = "abcdefghklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "1234567890";
        Random rand = new Random();
        int pos = rand.nextInt(randStr.length());

        return randStr.charAt(pos);
    }

    /**
     * Generates random and mutated String object.
     * @return object that is String value
     */
    private Object generateString()
    {
        String randStr = generateRandomString("abcdefghklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "1234567890" +
                "&*()_+-=\";:';<>/.,`");
        return String.valueOf(radamsaCall(randStr, "NONE"));
    }

    /**
     * Generates random String < 255 chars.
     * @return object that is String value
     */
    public String generateRandomString(String chars)
    {
        Random rand = new Random();
        int len = rand.nextInt(254) + 1;
        char[] text = new char[len];
        for (int i = 0; i < len; i++)
        {
            text[i] = chars.charAt(rand.nextInt(chars.length()));
        }
        return new String(text);
    }


    /**
     * Returns object value of specified class names
     * @param className class name as string
     * @param callStack stack of construction
     * @return generated value
     */
    public Object prepareTestObjectByClass(final String className, ArrayList<String> callStack) {
        // Check on cycle: if this type already in call stack, than return null.
        String trace = "";
        boolean isCycleFound = false;
        for (int i = 0; i < callStack.size(); i ++) {
            if (callStack.get(i).equals(className)) {
                isCycleFound = true;
            }
            trace += className + ", ";
        }

        if (isCycleFound) {
            trace += className;
            return null;
        }

        // In other cases add classname to call stack and keep working.
        callStack.add(className);

        //// There will be specific cases; classes which instance cannot be created automatically.
        // Cant be created otherway :(
        if (className.equals("android.content.Context")) {
            callStack.remove(callStack.size() - 1);
            return Controller.getContext();
        }

        // Cant be created otherway :(
        if (className.equals("java.lang.Class")) {
            callStack.remove(callStack.size() - 1);
            return generateClass();
        }

        // Cant be created otherway :(
        if (className.equals("android.os.Parcel")) {
            callStack.remove(callStack.size() - 1);
            Parcel p = Parcel.obtain();
            // Adding some salt.
            p.writeString((String) generateString());
            return p;
        }

        // Cant be created otherway :(
        if (className.equals("android.net.Uri")) {
            callStack.remove(callStack.size() - 1);
            return Uri.parse((String) generateString());
        }

        // Cant be created otherway :(
        if (className.equals("java.lang.CharSequence")) {
            callStack.remove(callStack.size() - 1);
            String s = "";
            CharSequence cs = s;
            return Uri.parse((String) generateString());
        }

        try {
            boolean isArray = false;
            String arrayElemsType = "";
            // Check if target is array, manually
            try {
                Class targetClass = Class.forName(className);
                if (targetClass.isArray()) {
                    isArray = true;
                    arrayElemsType = targetClass.getComponentType().getName();
                }
            } catch (ClassNotFoundException e) {
                if (className.contains("[]")) {
                    isArray = true;
                    arrayElemsType = className.replace("[]", "");
                }
            }
            // If class is array then prepare it with creating instances of element type
            if (isArray) {
                Class compClass = getType(arrayElemsType);
                // If target class is container than generate n (random 1..10) instances of that type
                Random rand = new Random();
                int len = rand.nextInt(9) + 1;

                Object instance = Array.newInstance(compClass, len);
                for (int i = 0; i < len; i ++) {
                    Object elem = generateValue(arrayElemsType, callStack);
                    Array.set(instance, i, elem);
                }

                callStack.remove(callStack.size() - 1);
                return instance;
            }

            // Getting java.lang.class from string
            Class<?> targetClass = Class.forName(className);

            // Check if target is generic
            if (targetClass.getTypeParameters().length > 0) {
                if (targetClass.getName().equals("java.util.List")) {
                    // If target class is container than generate n (random 1..10) instances of that type
                    Random rand = new Random();
                    int len = rand.nextInt(9) + 1;
                    Class compClass = generateClass();
                    ArrayList<Object> instance = new ArrayList<Object>();

                    for (int i = 0; i < len; i ++) {
                        Object elem = generateValue(compClass.getName(), callStack);
                        instance.add(i, elem);
                    }

                    callStack.remove(callStack.size() - 1);
                    return instance;
                }

                //Log.e("exception!", "unhandled generic class: " + targetClass );

                /*Set<? extends Class<?>> classes = reflectionUtil.getSubTypesOf(targetClass);
                Log.e("subtypes_count", classes.size() + "");
                Iterator<? extends Class<?>> iterator = classes.iterator();
                while (iterator.hasNext()) {
                    Class cl = iterator.next();
                    Log.e("SUBTYPE", "of " + targetClass.getName() + " is " + cl.getName());
                }*/
                //ParameterizedType genericType = (ParameterizedType) targetType;
            }

            Constructor<?> constructors[] = targetClass.getDeclaredConstructors();  // get all the constructors

            // Check if type has no constructors. It means that it is interface or abstract class.
            if (constructors.length == 0) {
                // Class with same name but without 'i' can implement target interface
                String possibleName = className.replace(".I", ".");
                Class possibleClass;
                ArrayList<Class> possibles = classStore;

                try {
                    possibleClass = Class.forName(possibleName);
                    possibles.add(possibleClass);
                } catch ( ClassNotFoundException e) {}

                for (int k = 0; k < possibles.size(); k ++) {
                    String candidate = possibles.get(k).getName();
                    if (targetClass.isAssignableFrom(possibles.get(k)))
                        if (tryToImplement(candidate)) {
                            Object instance = prepareTestObjectByClass(candidate, callStack);

                            callStack.remove(callStack.size() - 1);
                            return instance;
                        }
                }

                if (targetClass.isInterface()) {
                    // Constructing binder as stub. A lot of objects of Android implements IBinder interface.
                    Object instance = Proxy.newProxyInstance(targetClass.getClassLoader(), new Class[]{targetClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("toString"))
                                return "stub_" + className;

                            Binder binder = new Binder();
                            binder.attachInterface(null, className);

                            return binder;
                        }
                    });
                    callStack.remove(callStack.size() - 1);
                    return instance;

                } else {
                    callStack.remove(callStack.size() - 1);
                    return null;
                }
            // If type has constructors, than trying to create instance by it.
            } else {
                for (int i = 0; i < constructors.length; i++) {
                    Class[] argt = constructors[i].getParameterTypes();

                    Object instance = null;

                    if (argt.length == 0) {
                        try {
                            constructors[i].setAccessible(true);
                            instance = constructors[i].newInstance();
                        } catch (SecurityException e){
                            Log.w("Exception!", "cant make constructor accessible :(");
                            continue;
                        } catch (InstantiationException e) {
                            Log.w("Exception!", "cant instantiate :(");
                            continue;
                        }
                    } else {
                        try {
                            constructors[i].setAccessible(true);
                            Object[] argv = prepareParams(argt, callStack);
                            try {
                                DebugOutput.printArgs(argv);
                            } catch (NullPointerException e) {
                                Log.w("Exception!", "cant create one or more of args :(");
                                continue;
                            }

                            instance = constructors[i].newInstance(argv);
                        } catch (SecurityException e){
                            Log.w("Exception!", "cant make constructor accessible :(");
                            continue;
                        }
                    }

                    // Check if the created object is correct and can exist.
                    try {
                        Log.i("DEBUG", "" + instance);
                    } catch (IllegalStateException e) {
                        Log.e("DEBUG", ":( hm. than next constructor!");
                        continue;
                    } catch (NullPointerException e) {
                        Log.e("DEBUG", ":( hm. than next constructor!");
                        continue;
                    }
                    Log.e("DEBUG", "==========");

                    callStack.remove(callStack.size() - 1);
                    return instance;
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        callStack.remove(callStack.size() - 1);
        if (callStack.size() == 0)
            Log.e("EXCEPTION", className + " CANT INSTANTIATE THIS OBJ. NO WAY.");
        return null;
    }

    /**
     * Check if this type have constructors and correct instance can be created
     * @param className name of checking class as as string
     * @return can be implemented
     */
    boolean tryToImplement (String className) {
        try {
            Class classToImpl = Class.forName(className);
            Constructor<?> constructors[] = Class.forName(className).getDeclaredConstructors();
            if (constructors.length == 0 || Modifier.isAbstract(classToImpl.getModifiers()))
                return false;

        } catch (ClassNotFoundException e) {
            return false;
        }

        return true;
    }

    /**
     * Generating values by type-array
     * @param argt array of classes to be instantiated
     * @param callStack stack of construction
     * @return array of generated values
     */
    // Returns a list of arguments of considered constructor
    Object[] prepareParams(Class[] argt, ArrayList<String> callStack) {
        Object[] argv = new Object[argt.length];
        for (int i = 0; i < argt.length; i++) {
            argv[i] = generateValue(argt[i].getName(), callStack);
            if (argv[i] != null)
                continue;
            argv[i] = null;
        }
        return argv;
    }

    /**
     * Trying to get more realistic object with filling its fields with generated data.
     * @param target object that need to be mutated
     */
    public void fuzzInstance(Object target) {
        Field[] fields = target.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                // Log.i("fuzzing", "field " + i + " of type = " + fields[i].getType().getName() + "; name = " + fields[i].getName());
                fields[i].set(target, generateValue(fields[i].getType().getName(), new ArrayList<String>() {}));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * @return random class from class store
     */
    private Class generateClass()
    {
        Class instance;
        do {
            Random rand = new Random();
            int num = rand.nextInt(classStore.size());
            instance = classStore.get(num);
        } while (instance.isInterface());

        return instance;
    }
}
