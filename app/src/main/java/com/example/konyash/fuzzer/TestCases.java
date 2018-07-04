package com.example.konyash.fuzzer;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by voidgib on 06.07.17.
 */

public class TestCases {

    // DevicePolicyManager: wipeData || setGlobal proxy. Crash: "system ui crash".
    public static void testWipeData(MainActivity mainActivity)  {

        //Context context = this.getApplicationContext();
        //DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);

        Log.i("Crash", "is it repeatable result?");
        try {
            Object managerStub = mainActivity.getObj("android.app.admin.IDevicePolicyManager");

            Class[] argt = new Class[] {int.class, int.class};
            Object[] argv = new Object[] {new Integer(0).intValue(), new Integer(521593755).intValue()};
            mainActivity.invokeMethod(managerStub, "wipeData", argt, argv);
            Log.i("Crash", "int array - nope.");

            Object[] argv1 = new Object[] {521593755, 0};
            mainActivity.invokeMethod(managerStub, "wipeData", argt, argv1);
            Log.i("Crash", "half null array - nope.");

            Object[] argv2 = new Object[] {0, 0};
            mainActivity.invokeMethod(managerStub, "wipeData", argt, argv2);
            Log.i("Crash", "00 array - nope.");

            Object[] argv3 = new Object[] {0, 1};
            mainActivity.invokeMethod(managerStub, "wipeData", argt, argv3);
            Log.i("Crash", "01 array - nope.");

            Object[] argv4 = new Object[] {1, 0};
            mainActivity.invokeMethod(managerStub, "wipeData", argt, argv4);
            Log.i("Crash", "10 array - nope.");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //Log.i("TEST", line);

        //manager.wipeData(521593755, 0);
    }

    // Value generator: prepareTestObjectByClass. Test if it working method.
    public static void testObjectCreation(MainActivity mainActivity)  {
        //Log.e("INPUT:", line);

        //valGenerator.prepareTestObjectByClass(line, new ArrayList<String>());


               /* */


                /*
                Object temp = valGenerator.prepareTestObjectByClass(line, new ArrayList<String>());

                Log.e("RESULT:", temp.toString());*/
        //Log.e("step 2:", "fuzzing obj");

        //valGenerator.fuzzInstance(temp);
        //Log.e("RESULT:", temp.toString());
        //Log.e("done", "++++++++++++++++++++++");

                /*try {
                    InputStream iS = this.getResources().getAssets().open("test");
                    BufferedReader br = new BufferedReader(new InputStreamReader(iS));
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.equals("="))
                            break;
                        Log.e("INPUT:", line);
                        Object temp = valGenerator.prepareTestObjectByClass(line, new ArrayList<String>());

                        try {
                            Log.e("RESULT:", temp.toString());
                        } catch (NullPointerException e) {
                            Log.e("NORESULT:", "null");
                        }

                        //Log.e("step 2:", "fuzzing obj");

                        //valGenerator.fuzzInstance(temp);
                        //Log.e("RESULT:", temp.toString());
                        Log.e("done", "++++++++++++++++++++++");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
    }
}
