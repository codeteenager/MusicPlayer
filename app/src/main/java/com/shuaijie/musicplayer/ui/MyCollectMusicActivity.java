package com.shuaijie.musicplayer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.shuaijie.musicplayer.MyApplication;
import com.shuaijie.musicplayer.R;
import com.shuaijie.musicplayer.adapter.MyMusicListAdapter;
import com.shuaijie.musicplayer.bean.MusicInfo;
import com.shuaijie.musicplayer.service.PlayService;

import java.util.ArrayList;
import java.util.List;


public class MyCollectMusicActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView myCollectMusicList;
    private MyApplication myApplication;
    private ArrayList<MusicInfo> collectMusics;
    private MyMusicListAdapter myMusicListAdapter;

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unBindPlayService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication = (MyApplication) getApplication();
        setContentView(R.layout.activity_my_collect_music);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("我收藏的音乐");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        myCollectMusicList = (ListView) findViewById(R.id.lv_mycollect_music);
        initData();
    }

    private void initData() {
        try {
            List<MusicInfo> list = myApplication.dbUtils.findAll(Selector.from(MusicInfo.class).where("isLike", "=", "1"));
            if (list == null || list.size() == 0) {
                return;
            }
            collectMusics = (ArrayList<MusicInfo>) list;
            myMusicListAdapter = new MyMusicListAdapter(this, collectMusics);
            myCollectMusicList.setAdapter(myMusicListAdapter);
            myCollectMusicList.setOnItemClickListener(this);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.MY_COLLEXT) {
            playService.setMusicInfos(collectMusics);
            playService.setChangePlayList(PlayService.MY_COLLEXT);
        }
        playService.play(position);
        //保存播放的时间
        savePlayRecord();
    }

    private void savePlayRecord() {
        MusicInfo musicInfo = playService.getMusicInfos().get(playService.getCurrentPositon());
        try {
            MusicInfo playRecordMusicInfo = myApplication.dbUtils.findFirst(Selector.from(MusicInfo.class).where("musicInfoId", "=", musicInfo.getMusicInfoId()));
            if (playRecordMusicInfo == null) {
                musicInfo.setPlayTime(System.currentTimeMillis());
                myApplication.dbUtils.save(musicInfo);
            } else {
                playRecordMusicInfo.setPlayTime(System.currentTimeMillis());
                myApplication.dbUtils.update(playRecordMusicInfo, "playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
