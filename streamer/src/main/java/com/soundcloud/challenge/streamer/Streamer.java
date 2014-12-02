package com.soundcloud.challenge.streamer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Streamer {
    static {
        System.loadLibrary("streamer");
    }

    public static final String TAG = "Streamer";

    public native void startEvents();
    public native void stopEvents();
    public native String getInfo();

    public void onEvent(int eventType, String msg) {
        Log.i(TAG, "Type:" + eventType + " | " + msg);
        if (handler != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(LauncherActivity.KEY_ID, eventType);
            bundle.putString(LauncherActivity.KEY_TEXT, msg);

            Message theMessage = handler.obtainMessage(LauncherActivity.MESSAGE_EVENT);
            theMessage.setData(bundle);
            handler.sendMessage(theMessage);
        }
    }

    private Handler handler = null;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void removeHandler(Handler handler) {
        if (this.handler == handler) {
            this.handler = null;
        }
    }
}
