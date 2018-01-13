package com.xeecos.mitimelapse;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private int mCapturing;
    private int mFrames;
    private int mRestFrames;
    private int mDelay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCapturing = 0;
        mDelay = 1;
        mFrames = 1;
        mRestFrames = 0;
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
        }
        if(mFrames!=0){
            if(mRestFrames<=0){
                mCapturing = 0;
            }
        }
    }
}
