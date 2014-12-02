package com.soundcloud.challenge.streamer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;


public class LauncherActivity extends Activity implements UserSystenLogResolver {
    public static final String TAG = "LauncherActivity";

    public static final int MESSAGE_EVENT = 1;

    public static final String KEY_ID = "id";
    public static final String KEY_TEXT = "text";

    private TextView tvTest;
    private TextView tvUser;
    private TextView tvSystem;
    private TextView tvLog;
    private Streamer streamer;

    private int nUser;
    private int nSystem;
    private int nLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity_layout);
        tvTest = (TextView) findViewById(R.id.test);
        tvUser = (TextView) findViewById(R.id.value_user);
        tvSystem = (TextView) findViewById(R.id.value_system);
        tvLog = (TextView) findViewById(R.id.value_log);
        nUser = nSystem = nLog = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        streamer = new Streamer();
        tvTest.setText(streamer.getInfo());
    }

    @Override
    protected void onResume() {
        super.onResume();
        streamer.setHandler(handler);
        streamer.startEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        streamer.stopEvents();
        streamer.removeHandler(handler);
    }

    @Override
    public void onUserMessage(String text) {
        tvTest.setText(text);
        tvUser.setText("" + (++nUser));
    }

    @Override
    public void onSystemMessage(String text) {
        Log.i(TAG, "onSystemMesage()");
        tvTest.setText(text);
        tvSystem.setText("" + (++nSystem));
    }

    @Override
    public void onLogMessage(String text) {
        Log.i(TAG, "onLogMessage(\"" + text + "\")");
        tvLog.setText("" + (++nLog));
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MESSAGE_EVENT:
                    int id = message.getData().getInt(KEY_ID);
                    String text = message.getData().getString(KEY_TEXT);
                    MessageType.process(LauncherActivity.this, id, text);
                    break;
                default:
                    Log.w(TAG, "Received an unknown message type (" +
                            message.what +
                            ") via the handler.");
                    break;
            }
        }
    };
}
