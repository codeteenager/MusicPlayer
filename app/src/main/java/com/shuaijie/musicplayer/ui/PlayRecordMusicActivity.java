package com.shuaijie.musicplayer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.shuaijie.musicplayer.MyApplication;
import com.shuaijie.musicplayer.R;
import com.shuaijie.musicplayer.adapter.MyMusicListAdapter;
import com.shuaijie.musicplayer.bean.MusicInfo;
import com.shuaijie.musicplayer.service.PlayService;

import java.util.ArrayList;
import java.util.List;

public class PlayRecordMusicActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView myRecordMusicList;
    private MyApplication myApplication;
    private ArrayList<MusicInfo> recordMusics;
    private MyMusicListAdapter myMusicListAdapter;
    private TextView recordTip;

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
        setContentView(R.layout.activity_play_record_music);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("最近播放");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        myRecordMusicList = (ListView) findViewById(R.id.lv_myrecord_music);
        recordTip = (TextView) findViewById(R.id.tip_record);
        initData();
    }

    private void initData() {
        try {
            List<MusicInfo> list = myApplication.dbUtils.findAll(Selector.from(MusicInfo.class).where("playTime", "!=", "0").orderBy("playTime", true).limit(10));
            if (list == null || list.size() == 0) {
                recordTip.setVisibility(View.VISIBLE);
                myRecordMusicList.setVisibility(View.GONE);
            } else {
                recordTip.setVisibility(View.GONE);
                myRecordMusicList.setVisibility(View.VISIBLE);
                recordMusics = (ArrayList<MusicInfo>) list;
                myMusicListAdapter = new MyMusicListAdapter(this, recordMusics);
                myRecordMusicList.setAdapter(myMusicListAdapter);
                myRecordMusicList.setOnItemClickListener(this);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.MY_RECORD) {
            playService.setMusicInfos(recordMusics);
            playService.setChangePlayList(PlayService.MY_RECORD);
        }
        playService.play(position);
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
