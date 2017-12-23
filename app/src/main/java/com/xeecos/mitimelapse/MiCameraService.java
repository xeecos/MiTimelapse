package com.xeecos.mitimelapse;

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

import java.util.List;

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
        serviceInfo.notificationTimeout=100;
        setServiceInfo(serviceInfo);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String className = event.getClassName().toString();
        //AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

//        Log.d("tag",className+":"+event.getPackageName().toString());
//        if(source!=null) {
//            List<AccessibilityNodeInfo> infos = source.findAccessibilityNodeInfosByViewId("shutter_button");
//            if (infos != null) {
//                for (AccessibilityNodeInfo node : infos) {
//                    Log.d("tag", "class:" + node.getClassName().toString());
//                }
//            }
//        }
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //界面点击

//                    AccessibilityNodeInfo nodeInfo2 = getRootInActiveWindow();
//
//                    recycle(nodeInfo2);

                Log.d("tag", "click view");
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:

//                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:

                AccessibilityNodeInfo source = event.getSource();
                List<AccessibilityNodeInfo> infos = source.findAccessibilityNodeInfosByText("拍摄");
                if (infos != null) {
                    if(infos.size()>0){
                        try {
                            recycle(source);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
    private void onCapture(int time){
        if(isCapturing==1){
            return;
        }
        isCapturing = 1;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int middleYValue = displayMetrics.heightPixels * 7 / 8;
        Log.d("tag","height:"+displayMetrics.heightPixels);
        final int leftSideOfScreen = displayMetrics.widthPixels / 2-1;
        final int rightSizeOfScreen = leftSideOfScreen +1;
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(rightSizeOfScreen, middleYValue);


        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 1));
        final MiCameraService self = this;
        final int s = time;
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d("tag","Gesture Completed");
                super.onCompleted(gestureDescription);
                self.back(s);

            }
        }, null);
    }
    private void back(int s){

        try {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            Thread.sleep(s*1000);
            Log.d("tag","seconds:"+s);
            isCapturing = 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) throws InterruptedException {

        if (node == null) {
            return null;
        }
        if (node.getChildCount() == 0) {
//            if (node.getClassName().toString().equals("android.widget.TextView")) {
                if(node.getText()==null){
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }else {
                    Log.d("tag", "text:" + node.getText());
                    String str  = node.getText().toString();
                    if(str.contains("秒")){
                        String ss=str.substring(0,str.indexOf("秒"));
                        int s = Integer.parseInt(ss);
                        onCapture(s);
                    }
                }
//            }
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
    Log.d("tag","getKeyCode:"+event.getKeyCode());
        return super.onKeyEvent(event);
    }
}
