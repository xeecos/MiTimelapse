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

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
//                BuildConfig.APPLICATION_ID + ".provider",
//                createImageFile());
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                Uri.fromFile(photo));


//        Intent mIntent = new Intent();
//        ComponentName comp = new ComponentName(
//
//                "com.android.camera",
//                "com.android.camera.Camera");
//        mIntent.setComponent(comp);
//
//        startActivity(mIntent);

    }

    public void onClickCamera(View v) {
        startActivity(getPackageManager().getLaunchIntentForPackage("com.android.camera"));

    }
    public void onClick(View v) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
}
