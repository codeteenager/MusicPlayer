package com.shuaijie.musicplayer.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.shuaijie.musicplayer.MyApplication;

/**
 * Created by Administrator on 2016/2/2.
 */
public class AppUtils {
    public static void hideInputMethod(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) MyApplication.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
