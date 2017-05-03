package com.shuaijie.musicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shuaijie.musicplayer.service.PlayService;

public class MusicIntentReceiver extends BroadcastReceiver {
    PlayService playService = PlayService.getPlayService();

    public MusicIntentReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.media.AUDIO_BECOMING_NOISY")) {
            playService.pause();
        }
    }
}
