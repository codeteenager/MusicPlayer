package com.shuaijie.musicplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;

import com.shuaijie.musicplayer.MyApplication;
import com.shuaijie.musicplayer.R;
import com.shuaijie.musicplayer.bean.MusicInfo;
import com.shuaijie.musicplayer.ui.PlayActivity;
import com.shuaijie.musicplayer.utils.ContactUtils;
import com.shuaijie.musicplayer.utils.MediaUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener {
    private MediaPlayer mediaPlayer;
    public static final int NOTIFICATION_ID = 10;
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    private int play_mode = ORDER_PLAY;
    private int currentPositon;//当前正在播放的歌曲的位置
    public ArrayList<MusicInfo> musicInfos;
    private int ChangePlayList = MY_MUSIC;
    public static final int MY_MUSIC = 1;
    public static final int MY_COLLEXT = 2;
    public static final int MY_RECORD = 3;
    private MusicUpdateListener musicUpdateListener;
    private boolean isPause = false;//判断是否是暂停的
    private Random random = new Random();
    private RemoteViews remoteViews;
    private NotificationManager nm;
    private Notification notification;
    private Notification.Builder builder;
    public static PlayService playService;
    private ExecutorService es = Executors.newSingleThreadExecutor();

    public PlayService() {
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public long getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public static PlayService getPlayService() {
        return playService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        MyApplication myApplication = (MyApplication) getApplication();
        currentPositon = myApplication.sp.getInt("currentPosition", 0);
        play_mode = myApplication.sp.getInt("playMode", PlayService.ORDER_PLAY);
        musicInfos = MediaUtils.getMusicInfos(getApplicationContext());
        es.execute(updataStateRunnable);
        sendNotify();
        playService = this;
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void sendNotify() {

        builder = new Notification.Builder(getApplicationContext());
        if (!musicInfos.isEmpty()) {
            MusicInfo musicInfo = musicInfos.get(currentPositon);
            remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
            remoteViews.setTextViewText(R.id.notifySinger, musicInfo.getTitle());
            remoteViews.setTextViewText(R.id.notifyName, musicInfo.getArtist());
            builder.setTicker(musicInfo.getTitle());
            Intent prevIntent = new Intent(ContactUtils.PREV_MUSIC);
            PendingIntent prevPi = PendingIntent.getBroadcast(getApplicationContext(), 0, prevIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.notifyPrev, prevPi);

            Intent pauseIntent = new Intent(ContactUtils.PAUSE_MUSIC);
            PendingIntent pausePi = PendingIntent.getBroadcast(getApplicationContext(), 0, pauseIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.notifyPause, pausePi);

            Intent nextIntent = new Intent(ContactUtils.NEXT_MUSIC);
            PendingIntent nextPi = PendingIntent.getBroadcast(getApplicationContext(), 0, nextIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.notifyNext, nextPi);
        } else {
            remoteViews = new RemoteViews(getPackageName(), R.layout.notification_no_music);
            builder.setTicker("code音乐");
        }
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.mipmap.app_logo);
        builder.setOngoing(true);
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PlayActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = builder.build();
        startForeground(0, notification);
        nm.notify(PlayService.NOTIFICATION_ID, notification);
    }

    //更新通知栏状态
    public void updateNotify() {
        MusicInfo musicInfo = musicInfos.get(currentPositon);
        if (isPlaying()) {
            remoteViews.setImageViewResource(R.id.notifyPause, R.mipmap.pause);
        } else {
            remoteViews.setImageViewResource(R.id.notifyPause, R.mipmap.play);
        }
        remoteViews.setTextViewText(R.id.notifySinger, musicInfo.getTitle());
        remoteViews.setTextViewText(R.id.notifyName, musicInfo.getArtist());
        nm.notify(PlayService.NOTIFICATION_ID, notification);
    }

    public void cancleNotify() {
        stopForeground(true);
        nm.cancel(PlayService.NOTIFICATION_ID);
    }

    public int getCurrentPositon() {
        return currentPositon;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public ArrayList<MusicInfo> getMusicInfos() {
        return musicInfos;
    }

    public void setMusicInfos(ArrayList<MusicInfo> musicInfos) {
        this.musicInfos = musicInfos;
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }

    public int getChangePlayList() {
        return ChangePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        ChangePlayList = changePlayList;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(musicInfos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPositon);
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //获取焦点
                initMediaPlayer();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //长期失去焦点
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //失去焦点，但很快获取
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //允许低音量播放
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public class PlayBinder extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    Runnable updataStateRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && mediaPlayer != null && mediaPlayer.isPlaying()) {
                musicUpdateListener.onPublish(getCurrentProgress());
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public int getCurrentProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    //播放功能
    public void play(int position) {
        MusicInfo musicInfo = null;
        if (position < 0 || position >= musicInfos.size()) {
            position = 0;
        }
        musicInfo = musicInfos.get(position);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, Uri.parse(musicInfo.getUrl()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPositon = position;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (musicUpdateListener != null) {
            musicUpdateListener.onChange(currentPositon);
        }
        updateNotify();
    }

    //暂停功能
    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
        updateNotify();
        if (musicUpdateListener != null) {
            musicUpdateListener.onChange(currentPositon);
        }
    }

    //上一首
    public void prev() {
        if ((currentPositon - 1) < 0) {
            currentPositon = musicInfos.size() - 1;
        } else {
            currentPositon -= 1;
        }
        play(currentPositon);
    }

    //下一首
    public void next() {
        if ((currentPositon + 1) > (musicInfos.size() - 1)) {
            currentPositon = 0;
        } else {
            currentPositon += 1;
        }
        play(currentPositon);
    }

    //播放
    public void start() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            mediaPlayer.getDuration();
            updateNotify();
            if (musicUpdateListener != null) {
                musicUpdateListener.onChange(currentPositon);
            }
        }
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seek(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public interface MusicUpdateListener {
        public void onPublish(int progress);

        public void onChange(int position);
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancleNotify();
        if (es != null && !es.isShutdown()) {
            es.shutdown();
            es = null;
        }
        mediaPlayer = null;
        musicInfos = null;
        musicUpdateListener = null;
    }
}
