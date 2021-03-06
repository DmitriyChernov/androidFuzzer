package com.example.konyash.fuzzer.utils;

import android.util.Log;

/**
 * Created by voidgib on 30.08.18.
 */

public class AndroidServiceNames {

    /**
     * Converts service class name to service name
     * @param serviceClass service class name
     * @return service name needed by Binder to retrieve object.
     */
    public static String getServiceName(String serviceClass) {
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
}
