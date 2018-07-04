package com.example.konyash.fuzzer;

import android.app.Activity;
import android.content.Context;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;

import static android.R.attr.type;

public class MainActivity extends Activity implements View.OnClickListener {

    String aidlsFilename = "services";

    // View variables.
    TextView tvOut;
    Button btnFuzz;
    Button btnTest;
    Toast toast;

    // How much every method would invoke
    final int callCount = 30;

    // Logic variables.
    UDP_Client udpClient = new UDP_Client();
    static Value_Generator valGenerator;

    // Method that was invoked last.
    String currentMethod;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOut = (TextView) findViewById(R.id.tvOut);
        btnFuzz = (Button) findViewById(R.id.btnFuzz);
        btnTest = (Button) findViewById(R.id.btnTest);

        btnFuzz.setOnClickListener(this);
        btnTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFuzz:
                try {
                    fullTest();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnRead:
               try {
                    readFile();
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
            case R.id.btnTest:
                /*valGenerator = new Value_Generator(this);

                getSystemServicesFromContext();

                TestCases.testWipeData(this);*/
                break;
        }

    }

    private void fullTest() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ArrayList<String> ssNames = getSystemServicesFromContext();

        for (String ssName : ssNames) {
            Object sysService = this.getApplicationContext().getSystemService(ssName);

            try {
                udpClient.Message = "\n|||||||||||||||||||||\n"
                        + "Fuzzing started for "
                        + sysService.getClass().getName() + " class.\n";
                udpClient.sendMessage();
                Log.i("Object: ", "" + sysService);
            } catch (NullPointerException ex) {
                continue;
            }

            //DebugOutput.printDeclaredMethods(sysService.getClass());
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
                    invokeMethodByTypes(sysService, meth.getName(), tNames);
                }
            } catch (NullPointerException ex) {
            }
        }
    }

    // File to read consist of classes and their accessible methods.
    // Structure of this file described below:
    // ==
    // Class classname
    // 1 returnType method1(argtype1 arg1, ...)
    // ...
    // ==
    private void readFile() throws InstantiationException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ArrayList<String> classes = new ArrayList<String>();
        BufferedReader br = null;
        int num = 1;
        boolean supported = true;
        boolean prevSkiped = false;
        String className = "";

        valGenerator = new Value_Generator(this);


        try {

            String line;

            InputStream iS = this.getResources().getAssets().open(aidlsFilename);
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
                        obj = getObj(className);
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
                        String argsMsg = "(";
                        for (int i = 0; i < argTypes.size(); i++) {
                            argsMsg += argTypes.get(i);
                            if (i != argTypes.size() - 1) {
                                argsMsg += ", ";
                            }
                        }
                        argsMsg += ")";
                        for (int s = 0; s < callCount; s ++)
                        {
                            udpClient.Message = "\n============\n";
                            udpClient.Message += num + " method: " + meth + " on " +
                                    s + " iteration; with arg types: " + argsMsg
                                    + " started to test.\n";
                            udpClient.sendMessage();

                            invokeMethodByTypes(obj, meth, argTypes);
                            num++;
                        }
                    }
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
    }

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

    // Returns stub-like object of a class. Returning value is system service.
    public Object getObj(String cl) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

        // Print all accessible  methods for this class.
        /* Log.i("DEBUG", "ACCESSIBLE METHODS");
        Method[] accessibleMeths = targetObject.getClass().getDeclaredMethods();
        for (int i = 0; i < accessibleMeths.length; i++) {
            Log.i("Declared method " + accessibleMeths[i].getReturnType().getName() + ":", i + ")"
                    + accessibleMeths[i].getName());
            Type[] ts = accessibleMeths[i].getGenericParameterTypes();
            for (int j = 0; j < ts.length; j++) {
                Log.i("Arg:", j + "]" + ts[j].toString());
            }
        }
        Log.i("DEBUG", "==================="); */
        return targetObject;
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
            //Log.e("cause",  e.getCause().getMessage());
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
    private void invokeMethodByTypes(Object obj, String meth, ArrayList<String> types)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        int argc = types.size();

        for (int j = 0; j < callCount; j ++) {
            Object[] argv = new Object[argc];
            Class[] argt = new Class[argc];

            String argsMsg = argc + "(";
            for (int i = 0; i < argc; i ++ ) {
                try {
                    argv[i] = valGenerator.generateValue(types.get(i));
                    argt[i] = valGenerator.getType(types.get(i));
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

            }

            invokeMethod(obj, meth, argt, argv);
        }
    }

    // Returns service name needed by Binder to retrieve object.
    private String getServiceName(String serviceClass) {
        if (serviceClass.equals("IAccessibilityManager"))
            return "accessibility";
        if (serviceClass.equals("IKeystoreService"))
            return "android.security.keystore";
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
        if (serviceClass.equals("IBatteryManager"))
            return "batterymanager";
        if (serviceClass.equals("IBatteryPropertiesRegistrar"))
            return "batteryproperties";
        if (serviceClass.equals("IBatteryStats"))
            return "batterystats";
        if (serviceClass.equals("IBluetoothManager"))
            return "bluetooth";
        if (serviceClass.equals("ICameraService"))
            return "camera";
        if (serviceClass.equals("ICaptioningManager"))
            return "captioning";
        if (serviceClass.equals("ICarrierConfigManager"))
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
        if (serviceClass.equals("IDeviceIdleController"))
            return "deviceidle";
        if (serviceClass.equals("IDevicePolicyManager"))
            return "device_policy";
        if (serviceClass.equals("IDisplayManager"))
            return "display";
        if (serviceClass.equals("IDownloadManager"))
            return "download";
        if (serviceClass.equals("IDreamManager"))
            return "dream";
        if (serviceClass.equals("IDropBoxManagerService"))
            return "dropbox";
        if (serviceClass.equals("IEthernetManager"))
            return "ethernet";
        if (serviceClass.equals("IFingerprintManager"))
            return "fingerprint";
        if (serviceClass.equals("IHardwareService"))
            return "hardware";
        if (serviceClass.equals("IHdmiControlManager"))
            return "hdmi_control";
        if (serviceClass.equals("IInputMethodManager"))
            return "input_method";
        if (serviceClass.equals("IInputManager"))
            return "input";
        if (serviceClass.equals("ISms"))
            return "isms";
        if (serviceClass.equals("IJobScheduler"))
            return "jobscheduler";
        if (serviceClass.equals("IKeyguardManager"))
            return "keyguard";
        if (serviceClass.equals("ILauncherApps"))
            return "launcherapps";
        if (serviceClass.equals("ILayoutInflater"))
            return "layout_inflater";
        if (serviceClass.equals("ILocationManager"))
            return "location";
        if (serviceClass.equals("ILockSettings"))
            return "lock_settings";
        if (serviceClass.equals("IMediaProjectionManager"))
            return "media_projection";
        if (serviceClass.equals("IMediaRouter"))
            return "media_router";
        if (serviceClass.equals("IMediaSessionManager"))
            return "media_session";
        if (serviceClass.equals("IMidiManager"))
            return "midi";
        if (serviceClass.equals("INetworkStatsService"))
            return "netstats";
        if (serviceClass.equals("INetworkPolicyService"))
            return "netpolicy";
        if (serviceClass.equals("INetworkScoreManager"))
            return "network_score";
        if (serviceClass.equals("INfcManager"))
            return "nfc";
        if (serviceClass.equals("INotificationManager"))
            return  "notification";
        if (serviceClass.equals("INsdManager"))
            return "servicediscovery";
        if (serviceClass.equals("ITelephony"))
            return "phone";
        if (serviceClass.equals("IPersistentDataBlockManager"))
            return "persistent_data_block";
        if (serviceClass.equals("IPowerManager"))
            return "power";
        if (serviceClass.equals("IPrintManager"))
            return "print";
        if (serviceClass.equals("IRadioManager"))
            return "radio";
        if (serviceClass.equals("IRestrictionsManager"))
            return "restrictions";
        if (serviceClass.equals("IRttManager"))
            return "rttmanager";
        if (serviceClass.equals("ISearchManager"))
            return "search";
        if (serviceClass.equals("ISensorManager"))
            return "sensor";
        if (serviceClass.equals("ISerialManager"))
            return "serial";
        if (serviceClass.equals("ISipManager"))
            return "sip";
        if (serviceClass.equals("IStorageManager"))
            return "storage";
        if (serviceClass.equals("ITaskManager"))
            return "taskmanager";
        if (serviceClass.equals("ITelecomManager"))
            return "telecom";
        if (serviceClass.equals("ISubscriptionManager"))
            return "telephony_subscription_service";
        if (serviceClass.equals("ITextServicesManager"))
            return "textservices";
        if (serviceClass.equals("ITrustManager"))
            return "trust";
        if (serviceClass.equals("ITvInputManager"))
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
        if (serviceClass.equals("IVoiceInteractionManager"))
            return "voiceinteraction";
        if (serviceClass.equals("IWallpaperManager"))
            return "wallpaper";
        if (serviceClass.equals("IWifiP2pManager"))
            return "wifip2p";
        if (serviceClass.equals("IWifiManager"))
            return "wifi";
        if (serviceClass.equals("IWifiPasspointManager"))
            return "wifipasspoint";
        if (serviceClass.equals("IWifiScanner"))
            return "wifiscanner";
        if (serviceClass.equals("IWindowManager"))
            return "window";
        if (serviceClass.equals("IStatusBarManager"))
            return "statusbar";
        if (serviceClass.equals("IUpdateLock"))
            return "updatelock";
        if (serviceClass.equals("INetworkManagement"))
            return "network_management";
        Log.e("UNSUPPORTED MANAGER! ", serviceClass);
        throw new IllegalArgumentException();
    }

    public void setResp(String resp)
    {
        valGenerator.setResponce(resp);
    }

    public static int responceCount()
    {
        return valGenerator.responceQueueSize();
    }

    private boolean isMethodToSkip(String meth) {
        if (meth.equals("reboot")
                || meth.equals("shutdown")
                || meth.equals("clearApplicationUserData")
                || meth.equals("factoryReset")
                || meth.equals("disconnect"))
            return true;
        return  false;
    }

    public ArrayList<String> getSystemServicesFromContext() {
        ArrayList<String> services = new ArrayList<>();
        Context context = getApplicationContext();
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