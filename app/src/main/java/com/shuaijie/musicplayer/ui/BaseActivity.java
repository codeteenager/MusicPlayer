package com.shuaijie.musicplayer.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;

import com.shuaijie.musicplayer.MyApplication;
import com.shuaijie.musicplayer.service.PlayService;


public abstract class BaseActivity extends ActionBarActivity {
    public PlayService playService;
    private boolean isBind = false;

    private PlayService.MusicUpdateListener musicUpdateListener = new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    public abstract void publish(int progress);

    public abstract void change(int position);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder) service;
            playService = playBinder.getPlayService();
            playService.setMusicUpdateListener(musicUpdateListener);
            musicUpdateListener.onChange(playService.getCurrentPositon());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
        }
    };

    //绑定服务
    public void bindPlayService() {
        if (!isBind) {
            Intent intent = new Intent();
            intent.setAction("com.shuaijie.musicplayer.musicservice");
            intent.setPackage(getPackageName());
            bindService(intent, conn, BIND_AUTO_CREATE);
            isBind = true;
        }

    }

    //解除服务
    public void unBindPlayService() {
        if (isBind) {
            unbindService(conn);
            isBind = false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存当前播放的状态值
        MyApplication myApplication = (MyApplication) getApplication();
        SharedPreferences.Editor editor = myApplication.sp.edit();
        editor.putInt("currentPosition", playService.getCurrentPositon());
        editor.putInt("playMode", playService.getPlay_mode());
        editor.commit();
    }
}
