package com.xeecos.mitimelapse;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by indream on 2017/12/21.
 */

public class MiCameraService extends AccessibilityService {

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
                List<AccessibilityNodeInfo> infos = source.findAccessibilityNodeInfosByText("start_capture");//findAccessibilityNodeInfosByText("拍摄");
                if (infos != null) {
                    for (AccessibilityNodeInfo node : infos) {
                        if(node.getContentDescription()!=null) {
                            Log.d("tag", "text:" + node.getContentDescription().toString());
                        }
                        Log.d("tag", "class:" + node.getClassName().toString());
//                        node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                    }
                }
//                AccessibilityNodeInfo source = event.getSource();
                recycle(source);

//                recycle(source);
                break;
        }
    }
    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }
        if (node.getClassName().toString().equals( "android.widget.TextView")) {
            if(node.getText()==null){
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
//            Log.d("tag", "text:" + node.getText());
        }else{

        }
        if(node.getContentDescription()!=null) {
            Log.d("tag", "text:" + node.getContentDescription().toString() + " clickable:" + node.isClickable());
            if (node.getContentDescription().toString().equals("拍摄") && node.isClickable() && node.isEnabled()) {
                Log.d("tag", "click:" + node.getContentDescription().toString() + " class:" + node.getClassName());
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                return null;
            }
            List<AccessibilityNodeInfo.AccessibilityAction> infos = node.getActionList();
            if (infos != null) {
                for (AccessibilityNodeInfo.AccessibilityAction a : infos) {
                    Log.d("tag", "action:" + a.getClass().toString());
                }
            }
        }
        if (node.getChildCount() == 0) {

        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    recycle(node.getChild(i));
                }
            }
        }
        return node;
    }

}
