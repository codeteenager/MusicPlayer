package com.shuaijie.musicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shuaijie.musicplayer.service.PlayService;
import com.shuaijie.musicplayer.utils.ContactUtils;

public class MusicReceiver extends BroadcastReceiver {
    PlayService playService = PlayService.getPlayService();

    public MusicReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ContactUtils.PREV_MUSIC:
                playService.prev();
                break;
            case ContactUtils.NEXT_MUSIC:
                playService.next();
                break;
            case ContactUtils.PAUSE_MUSIC:
                if (playService.isPlaying()) {
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPositon());
                    }
                }
                break;
        }

    }
}
