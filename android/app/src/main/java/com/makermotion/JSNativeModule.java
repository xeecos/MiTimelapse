package com.makermotion;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.widget.Toast;
import android.content.Intent;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Environment;
import android.os.Handler;

import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.net.Uri;
import android.database.Cursor;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
    public void requestStorage(){
        verifyStoragePermissions(getCurrentActivity());
    }
    @ReactMethod
    public void openCamera(){ 
        // 
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
    private int mFrames;
    private List<String> getSystemPhotoList(Context context) {
        List<String> result = new ArrayList<String>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri,
                new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA },
                MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media._ID + " DESC");
        if (cursor == null || cursor.getCount() <= 0) {

            //            Log.i("tag", "empty!");
            return null; // 没有图片
        }
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(index); // 文件地址
            if (path.indexOf("Camera/IMG_") > -1) {
                File file = new File(path);
                if (file.exists()) {
                    if (result.size() < mFrames) {
                        result.add(path);
                        //                        Log.i("tag", path);
                    } else {
                        break;
                    }
                }
            }
        }
        return result;
    }

    private Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        if (bm != null & !bm.isRecycled()) {
            bm.recycle();
            bm = null;
        }
        return newbm;
    }
    

    private void sendMessage(String str) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("fromDevice", str);
    }
    @ReactMethod
    public void combineMovie(int frames) {
        mFrames = frames;
        final List<String> list = getSystemPhotoList(this.reactContext);
        
        // for Android use: AndroidSequenceEncoder
        new Thread(new Runnable() {
            @Override
            public void run() {
                SeekableByteChannel out = null;
                File appDir = new File(Environment.getExternalStorageDirectory(), "com.xeecos.mitimelapse");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                sendMessage("开始合成!");
                String fileName = System.currentTimeMillis() + ".mp4";
                File temp = new File(appDir, fileName);
                try {
                    out = NIOUtils.writableFileChannel(String.valueOf(temp));
                    AndroidSequenceEncoder encoder = new AndroidSequenceEncoder(out, Rational.R(25, 1));
                    int len = list.size();
                    for (int i = 0; i < len; i++) {
                        // Generate the image, for Android use Bitmap
                        FileInputStream fis = new FileInputStream(list.get(len - 1 - i));
                        Bitmap bitmap = scaleImage(BitmapFactory.decodeStream(fis), 1920, 1080);
                        int progress = (i + 1) * 100 / len;
                        sendMessage("处理中:" + progress + "%");
                        encoder.encodeImage(bitmap);
                        //                    Log.i("tag", "end processing:" + (i + 1));
                    }
                    // Finalize the encoding, i.e. clear the buffers, write the header, etc.
                    encoder.finish();
                } catch (FileNotFoundException e) {

                    //                    Log.i("tag","FileNotFoundException!");
                } catch (IOException e) {
                    e.printStackTrace();
                    //                    Log.i("tag","IOException!");
                } finally {
                    NIOUtils.closeQuietly(out);
                }
                sendMessage("合成完成!");
            }
        }).start();
    }
}