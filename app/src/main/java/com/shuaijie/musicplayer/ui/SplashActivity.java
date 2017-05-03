package com.shuaijie.musicplayer.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.shuaijie.musicplayer.R;

import java.lang.ref.WeakReference;


public class SplashActivity extends AppCompatActivity {
    private static final int START_MAIN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent();
        intent.setAction("com.shuaijie.musicplayer.musicservice");
        intent.setPackage(getPackageName());
        startService(intent);
        myHandler.sendEmptyMessageDelayed(START_MAIN, 2000);
    }

    private MyHandler myHandler = new MyHandler(this);

    private class MyHandler extends Handler {
        WeakReference<SplashActivity> weakReference;

        public MyHandler(SplashActivity splashActivity) {
            this.weakReference = new WeakReference<SplashActivity>(splashActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashActivity splashActivity = weakReference.get();
            switch (msg.what) {
                case START_MAIN:
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

}
