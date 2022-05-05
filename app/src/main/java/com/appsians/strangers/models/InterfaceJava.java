package com.appsians.strangers.models;

import android.webkit.JavascriptInterface;

import com.appsians.strangers.activities.CallActivity;

public class InterfaceJava {

    CallActivity callActivity;

    public InterfaceJava(CallActivity callActivity) {
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        callActivity.onPeerConnected();
    }
    @JavascriptInterface
    public  void endCallForced(){
        callActivity.endCallForced();
    }

}
