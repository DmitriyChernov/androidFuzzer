package com.example.konyash.fuzzer;

import android.app.Activity;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {

    TextView tvOut;
    Button btnOk;
    UDP_Client client = new UDP_Client();
    Toast toast;
    boolean methodsIsPrinted = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOut = (TextView) findViewById(R.id.tvOut);
        btnOk = (Button) findViewById(R.id.btnOk);

        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // по id определяем кнопку, вызвавшую этот обработчик
        switch (v.getId()) {
            case R.id.btnOk:
               try {
                    ArrayList<String> classes = readFile();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                   e.printStackTrace();
               } catch (NoSuchMethodException e) {
                   e.printStackTrace();
               } catch (IllegalAccessException e) {
                   e.printStackTrace();
               } catch (InvocationTargetException e) {
                   e.printStackTrace();
               }
                break;
        }
    }

    private ArrayList<String> readFile() throws InstantiationException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        toast = new Toast(getApplicationContext());
        ArrayList<String> classes = new ArrayList<String>();
        BufferedReader br = null;
        int num = 1;

        try {

            String line;

            InputStream iS = this.getResources().getAssets().open("classes");
            br = new BufferedReader(new InputStreamReader(iS));

            Object obj = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.matches("Class: .*")) {

                    line = line.replace("Class: ", ""); // Get classname
                    obj = getObj(line);
                    Log.i("Object: ", "" + obj);

                    client.Message = "======================\n";
                    client.sendMessage();
                } else if (line.matches("^\\d+.*")) {

                    String meth = "";
                    ArrayList<String> argTypes = new ArrayList<String>();

                    boolean isNameFinished = false;
                    for (int i = 0; i < line.length(); i ++ ) {
                        char ch = line.charAt(i);
                        if (((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
                                && !isNameFinished) {
                            String method = line.substring(i, line.length());
                            int endS = method.indexOf('(');
                            i+= endS;
                            String name = method.substring(0, endS);
                            name = name.substring(name.indexOf(' ') + 1, name.length());
                            meth = name;
                            isNameFinished = true;
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

                    String argsMsg = "";
                    for (int i = 0; i < argTypes.size(); i ++) {
                        argsMsg += argTypes.get(i) + ", ";
                    }
                    toast.cancel();
                    toast.makeText(this, "Fuzzing method: " + meth, Toast.LENGTH_LONG).show();
                    client.Message = num + " method: " + meth + " with args: " + argsMsg
                            + " started to test.\n";
                    client.sendMessage();
                    invokeMethod(obj, meth, argTypes);
                    num ++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return classes;
    }

    private Object getObj(String cl) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String serviceManagerName = "android.os.ServiceManager";
        String serviceManagerNativeName = "android.os.ServiceManagerNative";
        String targetObjName = cl;

        Class targetClass = Class.forName(targetObjName);
        Class targetStubClass = targetClass.getClasses()[0];
        Class serviceManagerClass = Class.forName(serviceManagerName);
        Class serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

        Object targetObject;
        Object serviceManagerObject;

        String targetName = getServiceName(targetClass.getSimpleName());

        Method getService = serviceManagerClass.getMethod("getService", String.class);

        Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
                "asInterface", IBinder.class);

        Binder tmpBinder = new Binder();
        tmpBinder.attachInterface(null, "fake");
        serviceManagerObject = tempInterfaceMethod.invoke(null,  new Object[] { tmpBinder });

        Log.i("target obj:", targetName);
        IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, targetName);

        Log.i("retbinder:", retbinder.toString());

        Method serviceMethod = targetStubClass.getMethod("asInterface", IBinder.class);
        targetObject = serviceMethod.invoke(null, new Object[]{retbinder});

        client.Message = "Fuzzing started for "
                + targetObject.getClass().getName() + " class.\n";
        client.sendMessage();

        // Print all accesible  methods.
        Log.i("DEBUG", "ACCESSIBLE METHODS");
        Method[] accesibleMeths = targetObject.getClass().getDeclaredMethods();
        for (int i = 0; i < accesibleMeths.length; i++) {
            Log.i("Declared method:", i + ")" + accesibleMeths[i].getName());
            Type[] ts = accesibleMeths[i].getGenericParameterTypes();
            for (int j = 0; j < ts.length; j++) {
                Log.i("Arg:", j + "]" + ts[j].toString());
            }
        }

        return targetObject;
    }

    private void invokeMethod(Object obj, String meth, ArrayList<String> types)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Log.i("invoke: ", " obj: " + obj
                + "\n method: " + meth);

        Class c = obj.getClass();

        int argc = types.size();
        Object[] argv = new Object[argc];
        Class[] argt = new Class[argc];
        Log.i("argc:", " " + argc);
        for (int i = 0; i < argc; i ++ ) {
            argv[i] = Value_Generator.generateValue(types.get(i));
            argt[i] = getType(types.get(i));
            Log.i("argType:", types.get(i) + ": " + argv[i]);
        }


        Method invokingMeth = obj.getClass().getDeclaredMethod(meth, argt);
        Object result = new Object();

        if (argc == 0) {
            Log.i("INVOKINGMETH", invokingMeth.getName());
            result = invokingMeth.invoke(obj, argv);
        } else {
            result = invokingMeth.invoke(obj, argv);
        }

        String argsMsg = "";
        for (int i = 0; i < argc; i ++) {
            argsMsg += argv[i] + ", ";
        }
        client.Message = "Method: " + meth + ", with parameters:" + argsMsg
                + " finished test with result: " + result + "\n";
        client.sendMessage();

        return;
    }


    private Class getType(String name)
    {
        Class res;
        try
        {
            res = Class.forName(name);
            return res;
        } catch (ClassNotFoundException e){
            if (name.equals("byte")) return byte.class;
            if (name.equals("short")) return short.class;
            if (name.equals("int")) return int.class;
            if (name.equals("long")) return long.class;
            if (name.equals("char")) return char.class;
            if (name.equals("float")) return float.class;
            if (name.equals("double")) return double.class;
            if (name.equals("boolean")) return boolean.class;
            if (name.equals("void")) return void.class;
        }
        return Object.class;
    }

    private String getServiceName(String serviceClass) {
        if (serviceClass.equals("IAccessibilityManager"))
            return "accessibility";
        if (serviceClass.equals("IAccountManager"))
            return "account";
        if (serviceClass.equals("IActivityManager"))
            return "activity";
        if (serviceClass.equals("IAlarmManager"))
            return "alarm";
        if (serviceClass.equals("IAppOpsService"))
            return "appops";
        if (serviceClass.equals("IAssetAtlas"))
            return "assetatlas";
        if (serviceClass.equals("IAppWidgetService"))
            return "appwidget";
        if (serviceClass.equals("IAudioService"))
            return "audio";
        if (serviceClass.equals("IBackupManager"))
            return "backup";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "batterymanager";
        if (serviceClass.equals("IBatteryPropertiesRegistrar"))
            return "batteryproperties";
        if (serviceClass.equals("IBatteryStats"))
            return "batterystats";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "bluetooth";
        if (serviceClass.equals("ICameraService"))
            return "camera";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "captioning";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "carrier_config";
        if (serviceClass.equals("IClipboard"))
            return "clipboard";
        if (serviceClass.equals("IConnectivityManager"))
            return "connectivity";
        if (serviceClass.equals("IContentService"))
            return "content";
        if (serviceClass.equals("IConsumerIrService"))
            return "consumer_ir";
        if (serviceClass.equals("ICountryDetector"))
            return "country_detector";
        if (serviceClass.equals("IDevicePolicyManager"))
            return "device_policy";
        if (serviceClass.equals("IDisplayManager"))
            return "display";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "download";
        if (serviceClass.equals("IDreamManager"))
            return "dream";
        if (serviceClass.equals("IDropBoxManagerService"))
            return "dropbox";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "fingerprint";
        if (serviceClass.equals("IHardwareService"))
            return "hardware";
        if (serviceClass.equals("IInputMethodManager"))
            return "input_method";
        if (serviceClass.equals("IInputManager"))
            return "input";
        if (serviceClass.equals("ISms"))
            return "isms";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "jobscheduler";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "keyguard";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "launcherapps";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "layout_inflater";
        if (serviceClass.equals("ILocationManager"))
            return "location";
        if (serviceClass.equals("ILockSettings"))
            return "lock_settings";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "media_projection";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "media_router";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "media_session";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "midi";
        if (serviceClass.equals("INetworkStatsService"))
            return "netstats";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "nfc";
        if (serviceClass.equals("INotificationManager"))
            return  "notification";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "servicediscovery";
        if (serviceClass.equals("IPowerManager"))
            return "power";
        if (serviceClass.equals("IPrintManager"))
            return "print";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "restrictions";
        if (serviceClass.equals("ISearchManager"))
            return "search";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "sensor";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "storage";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "taskmanager";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "telecom";
        if (serviceClass.equals("ITelephony"))
            return "phone";
        if (serviceClass.equals("ITextServicesManager"))
            return "textservices";
        if (serviceClass.equals("android.view.accessibility.IAccessibilityManager"))
            return "tv_input";
        if (serviceClass.equals("IUiModeManager"))
            return "uimode";
        if (serviceClass.equals("IUsageStats"))
            return "usagestats";
        if (serviceClass.equals("IUsbManager"))
            return "usb";
        if (serviceClass.equals("IUserManager"))
            return "user";
        if (serviceClass.equals("IVibratorService"))
            return "vibrator";
        if (serviceClass.equals("IWallpaperManager"))
            return "wallpaper";
        if (serviceClass.equals("IWifiP2pManager"))
            return "wifip2p";
        if (serviceClass.equals("IWifiManager"))
            return "wifi";
        if (serviceClass.equals("IWindowManager"))
            return "window";
        Log.e("unsupported manager", serviceClass);
        throw new IllegalArgumentException();
    }
}