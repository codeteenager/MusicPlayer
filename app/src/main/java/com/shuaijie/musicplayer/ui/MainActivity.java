package com.shuaijie.musicplayer.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.shuaijie.musicplayer.MyApplication;
import com.shuaijie.musicplayer.R;
import com.shuaijie.musicplayer.adapter.MenuAdapter;
import com.shuaijie.musicplayer.adapter.MyMusicListAdapter;
import com.shuaijie.musicplayer.bean.MusicInfo;
import com.shuaijie.musicplayer.service.PlayService;
import com.shuaijie.musicplayer.utils.MediaUtils;
import com.andraskindler.quickscroll.QuickScroll;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    public MyApplication myApplication;
    private ListView lv_mymusic;
    private ArrayList<MusicInfo> musicInfos;
    private MyMusicListAdapter myMusicListAdapter;
    private ImageView albumLogo, playPause, playNext;
    private TextView curMusicName, curMusicSinger;
    private QuickScroll quickScroll;
    private LinearLayout footerLayout;
    private RelativeLayout listLayout;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private ListView lvMenu;

    //更新进度条
    @Override
    public void publish(int progress) {

    }

    //切换状态
    @Override
    public void change(int position) {
        loadData();
        changUIStateOnPlay(position);
    }

    //加载数据
    public void loadData() {
        musicInfos = MediaUtils.getMusicInfos(this);
        if (myMusicListAdapter == null) {
            myMusicListAdapter = new MyMusicListAdapter(this, musicInfos);
            lv_mymusic.setAdapter(myMusicListAdapter);
        } else {
            myMusicListAdapter.setList(musicInfos);
        }

        lv_mymusic.setOnItemClickListener(this);
        if (musicInfos.isEmpty()) {
            footerLayout.setVisibility(View.GONE);
            listLayout.setVisibility(View.GONE);
        } else {
            footerLayout.setVisibility(View.VISIBLE);
            listLayout.setVisibility(View.VISIBLE);
        }
        initQuickScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();//绑定播放服务
    }

    @Override
    public void onPause() {
        super.onPause();
        unBindPlayService();//解除播放服务
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        lvMenu = (ListView) findViewById(R.id.lv_menu);
        lvMenu.setAdapter(new MenuAdapter(this));
        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, MyCollectMusicActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, PlayRecordMusicActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, FeedBackActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        break;
                }
            }
        });
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        myApplication = (MyApplication) getApplication();
        footerLayout = (LinearLayout) findViewById(R.id.footerLayout);
        listLayout = (RelativeLayout) findViewById(R.id.listLayout);
        lv_mymusic = (ListView) findViewById(R.id.lv_mymusic);
        albumLogo = (ImageView) findViewById(R.id.albumlogo);
        playPause = (ImageView) findViewById(R.id.play_pause2);
        playNext = (ImageView) findViewById(R.id.play_next2);
        curMusicName = (TextView) findViewById(R.id.curMusicName);
        curMusicSinger = (TextView) findViewById(R.id.curMusicSinger);
        playPause.setOnClickListener(this);
        playNext.setOnClickListener(this);
        albumLogo.setOnClickListener(this);
        registerForContextMenu(lv_mymusic);
        quickScroll = (QuickScroll) findViewById(R.id.quickscroll);
    }

    private void initQuickScroll() {
        quickScroll.init(QuickScroll.TYPE_POPUP_WITH_HANDLE, lv_mymusic, myMusicListAdapter, QuickScroll.STYLE_HOLO);
        quickScroll.setFixedSize(1);
        quickScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
        quickScroll.setPopupColor(QuickScroll.BLUE_LIGHT, QuickScroll.BLUE_LIGHT_SEMITRANSPARENT, 1, Color.WHITE, 1);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * 选项菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toggle.onOptionsItemSelected(item) | super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            case R.id.albumlogo:
                Intent intent = new Intent(this, PlayActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void savePlayRecord() {
        MusicInfo musicInfo = playService.getMusicInfos().get(playService.getCurrentPositon());
        try {
            MusicInfo playRecordMusicInfo = myApplication.dbUtils.findFirst(Selector.from(MusicInfo.class).where("musicInfoId", "=", musicInfo.getId()));
            if (playRecordMusicInfo == null) {
                musicInfo.setMusicInfoId(musicInfo.getId());
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

    public void changUIStateOnPlay(int position) {
        if (position >= 0 && position < playService.musicInfos.size()) {
            MusicInfo musicInfo = playService.musicInfos.get(position);
            curMusicName.setText(musicInfo.getTitle());
            curMusicSinger.setText(musicInfo.getArtist());
            if (playService.isPlaying()) {
                playPause.setImageResource(R.mipmap.pause);
            } else {
                playPause.setImageResource(R.mipmap.play);
            }
            Bitmap albumBitmap = MediaUtils.getArtwork(this, musicInfo.getId(), musicInfo.getAlbumId(), true);
            albumLogo.setImageBitmap(albumBitmap);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.MY_MUSIC) {
            playService.setMusicInfos(musicInfos);
            playService.setChangePlayList(PlayService.MY_MUSIC);
        }
        playService.play(position);
        //保存播放的时间
        savePlayRecord();
    }
}