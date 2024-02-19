package com.example.omidom.Models;

import android.webkit.JavascriptInterface;

public class InterfaceJava {
    private final OnPeerConnectedListener onPeerConnectedListener;

    public InterfaceJava(OnPeerConnectedListener listener) {
        this.onPeerConnectedListener = listener;
    }

    @JavascriptInterface
    public void onPeerConnected() {
        if (onPeerConnectedListener != null) {
            onPeerConnectedListener.onPeerConnected();
        }
    }
}
