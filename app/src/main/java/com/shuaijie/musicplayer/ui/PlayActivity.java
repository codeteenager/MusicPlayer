package com.shuaijie.musicplayer.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.shuaijie.musicplayer.MyApplication;
import com.shuaijie.musicplayer.R;
import com.shuaijie.musicplayer.bean.MusicInfo;
import com.shuaijie.musicplayer.service.PlayService;
import com.shuaijie.musicplayer.utils.MediaUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private TextView title, startTime, endTime;
    private SeekBar seekBar;
    private ImageView playMode, playPrev, playNext, playPause, playCollect;
    private static final int UPDATE_TIME = 100;
    private MyHandler myHandler = new MyHandler(this);
    private MyApplication myApplication;
    private LrcView mLrc;
    private String currentMusic;
    private MusicInfo musicInfo;
    private static PlayActivity playActivity;
    private List<String> lrcFile = new ArrayList<String>(); //lrc列表

//    public static PlayActivity getPlayActivity() {
//        if (playActivity == null) {
//            playActivity = new PlayActivity();
//        }
//        return playActivity;
//    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (playService.isPlaying()) {
                playService.pause();
                playService.seek(progress);
                playService.start();
                mLrc.changeCurrent(playService.getPosition());
            } else {
                playService.seek(progress);
                mLrc.changeCurrent(playService.getPosition());
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private static class MyHandler extends Handler {
        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.startTime.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }

    @Override
    public void publish(int progress) {
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
        seekBar.setProgress(progress);
        mLrc.changeCurrent(playService.getPosition());
    }

    @Override
    public void change(int position) {
        musicInfo = playService.musicInfos.get(position);
        if (currentMusic == null) {
            currentMusic = musicInfo.getTitle();
            for (String file : lrcFile) {
                if (file.contains(musicInfo.getTitle())) {
                    try {
                        mLrc.setLrcPath(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } else {
            if (!currentMusic.equals(musicInfo.getTitle())) {
                for (String file : lrcFile) {
                    if (file.contains(musicInfo.getTitle())) {
                        try {
                            mLrc.setLrcPath(file);
                            mLrc.changeCurrent(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        title.setText(musicInfo.getTitle());
        endTime.setText(MediaUtils.formatTime(musicInfo.getDuration()));
        if (playService.isPlaying()) {
            playPause.setImageResource(R.mipmap.pause);
        } else {
            playPause.setImageResource(R.mipmap.play);
        }
        seekBar.setProgress(0);
        seekBar.setMax((int) musicInfo.getDuration());
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                playMode.setImageResource(R.mipmap.order);
                playMode.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                playMode.setImageResource(R.mipmap.random);
                playMode.setTag(PlayService.RANDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                playMode.setImageResource(R.mipmap.single);
                playMode.setTag(PlayService.SINGLE_PLAY);
                break;
        }
        Bitmap albumBitmap = MediaUtils.getArtwork(this, musicInfo.getId(), musicInfo.getAlbumId(), true);
        try {
            MusicInfo collectMusic = myApplication.dbUtils.findFirst(Selector.from(MusicInfo.class).where("musicInfoId", "=", getId(musicInfo)));
            if (collectMusic != null) {
                if (collectMusic.getIsLike() == 1) {
                    playCollect.setImageResource(R.mipmap.heart_select);
                } else {
                    playCollect.setImageResource(R.mipmap.heart_normal);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    public void GetFiles(String Path, String Extension, boolean IsIterative) //搜索目录，扩展名，是否进入子文件夹
    {
        File[] files = new File(Path).listFiles();

        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isFile()) {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension)) //判断扩展名
                    lrcFile.add(f.getPath());
                if (!IsIterative)
                    break;
            } else if (f.isDirectory() && f.getPath().indexOf("/.") == -1) //忽略点文件（隐藏文件/文件夹）
                GetFiles(f.getPath(), Extension, IsIterative);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        initView();
        myApplication = (MyApplication) getApplication();
    }

    private void initView() {
        mLrc = (LrcView) findViewById(R.id.lrc);
        title = (TextView) findViewById(R.id.tv_play_name);
        startTime = (TextView) findViewById(R.id.start_time);
        endTime = (TextView) findViewById(R.id.end_time);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        playMode = (ImageView) findViewById(R.id.play_mode);
        playPrev = (ImageView) findViewById(R.id.play_prev);
        playNext = (ImageView) findViewById(R.id.play_next2);
        playPause = (ImageView) findViewById(R.id.play_pause2);
        playCollect = (ImageView) findViewById(R.id.play_collect);
        playNext.setOnClickListener(this);
        playPrev.setOnClickListener(this);
        playPause.setOnClickListener(this);
        playMode.setOnClickListener(this);
        playCollect.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        bindPlayService();
        GetFiles(Environment.getExternalStorageDirectory() + "/Music", ".lrc", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();//绑定播放服务
    }

    @Override
    protected void onPause() {
        super.onPause();
        unBindPlayService();//解除播放服务
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private long getId(MusicInfo musicInfo) {
        long id = 0;
        switch (playService.getChangePlayList()) {
            case PlayService.MY_MUSIC:
                id = musicInfo.getId();
                break;
            case PlayService.MY_COLLEXT:
                id = musicInfo.getMusicInfoId();
                break;
        }
        return id;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pause2:
                if (playService.isPlaying()) {
                    playPause.setImageResource(R.mipmap.play);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        playPause.setImageResource(R.mipmap.pause);
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPositon());
                    }
                }
                break;
            case R.id.play_next2:
                playService.next();
                break;
            case R.id.play_prev:
                playService.prev();
                break;
            case R.id.play_mode:
                int mode = (int) playMode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        playMode.setImageResource(R.mipmap.random);
                        playMode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        playMode.setImageResource(R.mipmap.single);
                        playMode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.SINGLE_PLAY:
                        playMode.setImageResource(R.mipmap.order);
                        playMode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.play_collect:
                MusicInfo musicInfo = playService.musicInfos.get(playService.getCurrentPositon());
                try {
                    MusicInfo collectMusic = myApplication.dbUtils.findFirst(Selector.from(MusicInfo.class).where("musicInfoId", "=", getId(musicInfo)));
                    if (collectMusic == null) {
                        musicInfo.setMusicInfoId(musicInfo.getId());
                        musicInfo.setIsLike(1);
                        myApplication.dbUtils.save(musicInfo);
                        playCollect.setImageResource(R.mipmap.heart_select);
                    } else {
                        int isLike = collectMusic.getIsLike();
                        if (isLike == 1) {
                            collectMusic.setIsLike(0);
                            playCollect.setImageResource(R.mipmap.heart_normal);
                        } else {
                            collectMusic.setIsLike(1);
                            playCollect.setImageResource(R.mipmap.heart_select);
                        }
                        myApplication.dbUtils.update(collectMusic, "isLike");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(PlayActivity.this, MainActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(PlayActivity.this, MainActivity.class));
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
