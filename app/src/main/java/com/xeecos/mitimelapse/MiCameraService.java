package com.xeecos.mitimelapse;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
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
        Log.d("tag",className+":"+eventType);
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//        List<AccessibilityNodeInfo> infos =  nodeInfo.findAccessibilityNodeInfosByViewId("shutter_button");
//        if(infos!=null) {
//            for (AccessibilityNodeInfo node : infos) {
//                Log.d("tag", "class:" + node.getClassName().toString());
//            }
//        }
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (nodeInfo != null) {
                    recycle(nodeInfo);
                }
                break;
        }
    }
    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) {


        Log.d("tag", "text:"+node.getClassName());
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
