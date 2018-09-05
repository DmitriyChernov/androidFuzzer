package com.example.konyash.fuzzer.fuzzer;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.konyash.fuzzer.Control.Controller;
import com.example.konyash.fuzzer.utils.AndroidServiceNames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by voidgib on 30.08.18.
 */

public class AIDLFuzzer extends IFuzzer  {

    // Method that was invoked last.
    private String currentMethod;

    public AIDLFuzzer(Controller controller) {
        super(controller);
    }


    /** File to read consist of classes and their accessible methods.
    * Structure of this file described below:
    * ==
    * Class classname
    * 1 returnType method1(argtype1 arg1, ...)
    * ...
    * ==
    */
    public void fuzz() throws InstantiationException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BufferedReader br = null;
        boolean prevSkiped = false;
        String className = "";

        try {

            String line;

            InputStream iS = Controller.getAIDLFile();
            br = new BufferedReader(new InputStreamReader(iS));

            Object obj = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // If current string contains classname or it skiped,
                // then trying to get instance of a class.
                if (line.matches("Class: .*") || prevSkiped) {
                    if (!prevSkiped) {
                        className = line.replace("Class: ", ""); // Get classname
                    }
                    try {
                        obj = getStubService(className);
                        udpClient.Message = "\n|||||||||||||||||||||\n"
                                + "Fuzzing started for "
                                + obj.getClass().getName() + " class.\n";
                        udpClient.sendMessage();
                        Log.i("Object: ", "" + obj);
                    } catch (Exception e) {
                        String msg = "Cannot retrieve instance of a class: " + className;
                        udpClient.Message = msg;
                        udpClient.sendMessage();

                        Log.e("getOBJECT:", msg);

                        className = skipMethodsDescription(br);
                    }
                }

                // If current string contains description of some method...
                if (line.matches("^\\d+.*")) {

                    String meth = "";
                    ArrayList<String> argTypes = new ArrayList<String>();

                    boolean isNameFinished = false;
                    for (int i = 0; i < line.length(); i++) {
                        char ch = line.charAt(i);
                        // Retrieving method name
                        if (((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
                                && !isNameFinished) {
                            String method = line.substring(i, line.length());
                            int endS = method.indexOf('(');
                            i += endS;
                            String name = method.substring(0, endS);
                            name = name.substring(name.indexOf(' ') + 1, name.length());
                            meth = name;
                            isNameFinished = true;
                            // Retrieving method arguments
                        } else if (isNameFinished) {
                            try {
                                String types = line.substring(i, line.length() - 3);
                                String[] tokens = types.split(",");
                                for (String token : tokens) {
                                    token = token.trim();
                                    String type = token.substring(0, token.indexOf(' '));
                                    argTypes.add(type);
                                }
                                break;
                            } finally {
                                break;
                            }
                        }
                    }

                    currentMethod = meth;
                    if (!isMethodToSkip(meth)) {
                        invoker.invokeMethodByTypes(obj, meth, argTypes);
                    }
                }
            }

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Skipping description of a method that can be in aidls file
     * @param br reader of aidl file
     * @return classname as string
     */
    private String skipMethodsDescription(BufferedReader br)
    {
        String line;
        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Next class
                if (line.matches("Class: .*")) {
                    return line.replace("Class: ", "");
                } else {
                    // Skip line with method description
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "EOF";
    }

    /**
     * Getting stub-like system service from context
     * @param cl class name of a service
     * @return stub-like object of a class. Returning value is system service.
     */
    public Object getStubService(String cl) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String serviceManagerName = "android.os.ServiceManager";
        String serviceManagerNativeName = "android.os.ServiceManagerNative";
        String targetObjName = cl;

        Class targetClass = Class.forName(targetObjName);
        Class targetStubClass = targetClass.getClasses()[0];
        Class serviceManagerClass = Class.forName(serviceManagerName);
        Class serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

        Object targetObject;
        Object serviceManagerObject;

        String targetName = AndroidServiceNames.getServiceName(targetClass.getSimpleName());

        Method getService = serviceManagerClass.getMethod("getService", String.class);

        Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
                "asInterface", IBinder.class);

        Binder tmpBinder = new Binder();
        tmpBinder.attachInterface(null, targetObjName);
        serviceManagerObject = tempInterfaceMethod.invoke(null,  new Object[] { tmpBinder });

        IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, targetName);
        Method serviceMethod = targetStubClass.getMethod("asInterface", IBinder.class);
        targetObject = serviceMethod.invoke(null, new Object[]{retbinder});

        // Logging placed here because in this area short name of class is visible.
        udpClient.Message = "\n|||||||||||||||||||||\n"
                + "Fuzzing started for "
                + targetObject.getClass().getName() + " class.\n";
        udpClient.sendMessage();

        return targetObject;
    }
}
