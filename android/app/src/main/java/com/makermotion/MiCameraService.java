package com.makermotion;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

/**
 * Created by indream on 2017/12/21.
 */

public class MiCameraService extends AccessibilityService {

    private int isCapturing = 0;
    @Override
    public void onInterrupt() {  }
    @Override
    protected void onServiceConnected() {
        Log.d("tag","onServiceConnected");
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.packageNames = new String[]{"com.android.camera"};
        serviceInfo.notificationTimeout=1;
        setServiceInfo(serviceInfo);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String className = event.getClassName().toString();
        
        Log.d("tag","service entered!");
        AccessibilityNodeInfo source = event.getSource();
        try {
            if(isCapturing ==1){
                return;
            }
            recycle(source);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                final AccessibilityEvent evt = event;

                break;
        }
    }
    private void onCapture(int time){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.d("tag","height:"+displayMetrics.heightPixels);
        final int bottomY = displayMetrics.heightPixels * 7 / 8;
        final int centerX = displayMetrics.widthPixels / 2;
        final int centerY = displayMetrics.heightPixels / 2;
        delayCall(10,new DelayInterface(){
            @Override
            public void callback() {
                clickScreen(centerX,centerY);
            }
        });
        delayCall(50,new DelayInterface(){
            @Override
            public void callback() {
                clickScreen(centerX,bottomY);
            }
        });
        delayCall((time * 1000+800), new DelayInterface() {
            @Override
            public void callback() {
                back();
            }
        });
       Log.d("tag","seconds:"+time);
    }
    interface DelayInterface {
        void callback();
    }
    private void delayCall(int time, final DelayInterface incc){
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        incc.callback();
                    }
                },
                time);
    }
    private void clickScreen(int x,int y){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x, y);
        Log.d("tag","click:"+x+":"+y);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 10));
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
//                Log.i("tag","Gesture Completed");
                super.onCompleted(gestureDescription);
            }
        }, null);
    }
    private void back() {
        isCapturing = 0;
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }
    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) throws InterruptedException {

        if (node == null) {
            return null;
        }
        if (node.getChildCount() == 0) {
                if(node.getText()==null){
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if(node.getContentDescription()!=null){
                        Log.d("tag", "desc:" + node.getContentDescription());
//                        if(node.getContentDescription().equals("拍摄")) {
//                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        }
                    }
                }else {
                    Log.d("tag", "text:" + node.getText());
                    final String str  = node.getText().toString();
                    if(str.contains("秒")){
                        isCapturing = 1;
                        String ss=str.substring(0,str.indexOf("秒"));
                        try {
                            Integer s = Integer.parseInt(ss);
                            onCapture(s);
                        }catch (Exception e){
                            onCapture(1);
                        }
                        return null;
                    }
                }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    recycle(node.getChild(i));
                }
            }
        }
        return node;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return super.onKeyEvent(event);
    }
}
