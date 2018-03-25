package com.makermotion;

import android.app.Activity;
import android.widget.Toast;
import android.content.Intent;
import android.provider.Settings;
import android.os.Handler;

import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.HashMap;
import java.util.Map;

public class JSNativeModule extends ReactContextBaseJavaModule {

    private static final String DURAION_SHORT_KEY = "SHORT";
    private static final String DURAION_LONG_KEY = "LONG";
    private ReactApplicationContext reactContext;
    public JSNativeModule(ReactApplicationContext reactApplicationContext) {
        super(reactApplicationContext);
        this.reactContext = reactApplicationContext;
    }

    @Override
    public String getName() {
        return "RNMiService";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DURAION_SHORT_KEY, Toast.LENGTH_SHORT);
        constants.put(DURAION_LONG_KEY, Toast.LENGTH_LONG);
        return constants;
    }

    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, duration).show();

    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = { "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String[] PERMISSIONS_CAMERA = { "android.permission.CAMERA" };
    @ReactMethod
    public void openCamera(){ 
        // verifyStoragePermissions(getCurrentActivity());
        try {
            int permission = ActivityCompat.checkSelfPermission(getCurrentActivity(), "android.permission.CAMERA");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getCurrentActivity(), PERMISSIONS_CAMERA, 1);
            }else{
                this.reactContext.startActivity(this.reactContext.getPackageManager().getLaunchIntentForPackage("com.android.camera"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    @ReactMethod
    public void openService(){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
         this.reactContext.startActivity(intent);
    }
    @ReactMethod
    public void capture() {
        this.reactContext.startActivity(this.reactContext.getPackageManager().getLaunchIntentForPackage("com.android.camera"));
    }
}