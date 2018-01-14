package com.xeecos.mitimelapse;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int mCapturing;
    private int mFrames;
    private int mRestFrames;
    private int mDelay;
    private PowerManager.WakeLock wakeLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCapturing = 0;
        mDelay = 1;
        mFrames = 1;
        mRestFrames = 0;
        verifyStoragePermissions(this);
        wakeUp();
    }
    protected void onDestroy(){
        if(wakeLock!=null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }
    private void wakeUp(){
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK, "==KeepScreenOn==");
        wakeLock.acquire();
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    public static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if(mCapturing==1){
            continueCapturing();
        }
    }
    public void onClickTest(View v) {
        startActivity(getPackageManager().getLaunchIntentForPackage("com.android.camera"));

    }
    public void onClickSetting(View v) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    public void onClickStart(View v) {
        Button bt = (Button)findViewById(R.id.buttonStart);
        if(bt.getText().equals("Start")) {
            bt.setText("Stop");
            mCapturing = 1;
            Spinner delaySpinner = (Spinner) findViewById(R.id.spinner_delay);
            Spinner framesSpinner = (Spinner) findViewById(R.id.spinner_frames);
            String delayStr = String.valueOf(delaySpinner.getSelectedItem());
            String framesStr = String.valueOf(framesSpinner.getSelectedItem());
            mDelay = Integer.parseInt(delayStr);
            mFrames = Integer.parseInt(framesStr);
            mRestFrames = mFrames;
            continueCapturing();
        }else{
            mCapturing = 0;
            bt.setText("Start");
        }
    }
    public void onClickCombine(View v) {
        Spinner delaySpinner = (Spinner) findViewById(R.id.spinner_delay);
        Spinner framesSpinner = (Spinner) findViewById(R.id.spinner_frames);
        String delayStr = String.valueOf(delaySpinner.getSelectedItem());
        String framesStr = String.valueOf(framesSpinner.getSelectedItem());
        mDelay = Integer.parseInt(delayStr);
        mFrames = Integer.parseInt(framesStr);
        combineMovie();
    }
    private List<String> getSystemPhotoList(Context context)
    {
        List<String> result = new ArrayList<String>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA}, MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?", new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media._ID + " DESC");
        if (cursor == null || cursor.getCount() <= 0) {

            Log.d("tag", "empty!");
            return null; // 没有图片
        }
        while (cursor.moveToNext())
        {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(index); // 文件地址
            if(path.indexOf("Camera/IMG_")>-1) {
                File file = new File(path);
                if (file.exists()) {
                    if(result.size()<mFrames) {
                        result.add(path);
                        Log.d("tag", path);
                    }else{
                        break;
                    }
                }
            }
        }
        return result ;
    }
    private Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight)
    {
        if (bm == null)
        {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        if (bm != null & !bm.isRecycled())
        {
            bm.recycle();
            bm = null;
        }
        return newbm;
    }
    Handler handle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
             TextView tv = findViewById(R.id.textView);
            tv.setText(msg.obj.toString());
            Log.d("tag",msg.obj.toString());
        }

    };
    private void sendMessage(String str){
        Message msg = new Message();
        msg.obj = str;
        handle.sendMessage(msg);
    }
    public void onClickOpen(View v){
        File appDir = new File(Environment.getExternalStorageDirectory(),"com.xeecos.mitimelapse");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(appDir), "*/*");
        startActivity(Intent.createChooser(intent, "Open Folder"));
    }
    private void combineMovie(){
        final List<String> list = getSystemPhotoList(this);
        // for Android use: AndroidSequenceEncoder
        new Thread(new Runnable() {
            @Override
            public void run() {
                SeekableByteChannel out = null;
                File appDir = new File(Environment.getExternalStorageDirectory(),"com.xeecos.mitimelapse");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                sendMessage("start!");
                String fileName = System.currentTimeMillis() + ".mp4";
                File temp = new File(appDir, fileName);
                try {
                out = NIOUtils.writableFileChannel(String.valueOf(temp));
                AndroidSequenceEncoder encoder = new AndroidSequenceEncoder(out, Rational.R(25, 1));
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    // Generate the image, for Android use Bitmap
                    FileInputStream fis = new FileInputStream(list.get(i));
                    Bitmap bitmap = scaleImage(BitmapFactory.decodeStream(fis), 1920, 1080);
                    int progress = (i + 1) * 100 / len;
                    sendMessage("processing:" + progress + "%");
                    encoder.encodeImage(bitmap);
                    Log.d("tag", "end processing:" + (i + 1));
                }
                // Finalize the encoding, i.e. clear the buffers, write the header, etc.
                encoder.finish();
                } catch(FileNotFoundException e){

                    Log.d("tag","FileNotFoundException!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("tag","IOException!");
                } finally {
                    NIOUtils.closeQuietly(out);
                }
                sendMessage("finish!");
            }
        }).start();
    }
    private void continueCapturing(){
        if(mRestFrames>0||mFrames==0) {
            mRestFrames--;
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            if(mCapturing == 1) {
                                startActivity(getPackageManager().getLaunchIntentForPackage("com.android.camera"));
                            }
                        }
                    }, mDelay * 1000 + 500);
            TextView tv = findViewById(R.id.textView);
            tv.setText("total: "+mFrames+" capturing:"+(mFrames-mRestFrames));
        }
        if(mFrames!=0){
            if(mRestFrames<=0){
                mCapturing = 0;
                Button bt = (Button)findViewById(R.id.buttonStart);
                bt.setText("Start");
            }
        }
    }
}
