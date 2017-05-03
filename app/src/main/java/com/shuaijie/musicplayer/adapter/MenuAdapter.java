package com.shuaijie.musicplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuaijie.musicplayer.R;

public class MenuAdapter extends BaseAdapter {
    private String[] menu = {"我喜欢", "最近播放", "意见反馈", "关于我们"};
    private int[] resId = {R.mipmap.heart_normal, R.mipmap.record_logo, R.mipmap.logo_feedback, R.mipmap.logo_about};
    private Context context;

    public MenuAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return menu.length;
    }

    @Override
    public Object getItem(int i) {
        return menu[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View menuItem = View.inflate(context, R.layout.menu_item, null);
        TextView tv_menu = (TextView) menuItem.findViewById(R.id.tv_menu);
        ImageView iv_menu = (ImageView) menuItem.findViewById(R.id.iv_menu);
        iv_menu.setImageResource(resId[i]);
        tv_menu.setText(menu[i]);
        return menuItem;
    }
}

