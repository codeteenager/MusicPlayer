package com.shuaijie.musicplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;
import com.shuaijie.musicplayer.utils.ContactUtils;


public class MyApplication extends Application {
    public SharedPreferences sp;
    public DbUtils dbUtils;//第三方框架xutils
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(ContactUtils.SP_NAME, Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(), ContactUtils.DB_NAME);
        context = getApplicationContext();
    }

}
