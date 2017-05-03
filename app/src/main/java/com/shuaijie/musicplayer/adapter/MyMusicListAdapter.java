package com.shuaijie.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.shuaijie.musicplayer.R;
import com.shuaijie.musicplayer.bean.MusicInfo;
import com.shuaijie.musicplayer.utils.MediaUtils;

import java.util.ArrayList;


public class MyMusicListAdapter extends BaseAdapter implements Scrollable {
    private Context context;
    private ArrayList<MusicInfo> list;

    public MyMusicListAdapter(Context context, ArrayList<MusicInfo> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(ArrayList<MusicInfo> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music, null);
            vh = new ViewHolder();
            vh.musicName = (TextView) convertView.findViewById(R.id.music_name);
            vh.musicSinger = (TextView) convertView.findViewById(R.id.music_singer);
            vh.musicTime = (TextView) convertView.findViewById(R.id.music_time);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        MusicInfo musicInfo = list.get(position);
        vh.musicTime.setText(MediaUtils.formatTime(musicInfo.getDuration()));
        vh.musicName.setText(musicInfo.getTitle());
        vh.musicSinger.setText(musicInfo.getArtist());
        return convertView;
    }

    @Override
    public String getIndicatorForPosition(int childposition, int groupposition) {
        return Character.toString(list.get(childposition).getTitle().charAt(0));
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }

    private static class ViewHolder {
        TextView musicName, musicSinger, musicTime;
    }
}
