package ru.krivocraft.tortoise.android;

import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.function.BiFunction;

public class PlaybackState implements BiFunction<Integer, Integer, PlaybackStateCompat> {

    @Override
    public PlaybackStateCompat apply(Integer state, Integer position) {
        long availableActions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_STOP;

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            availableActions |= PlaybackStateCompat.ACTION_PAUSE;
        } else if (state == PlaybackStateCompat.STATE_PAUSED) {
            availableActions |= PlaybackStateCompat.ACTION_PLAY;
        }

        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder()
                .setActions(availableActions)
                .setState(state, position, 1, SystemClock.elapsedRealtime());
        return builder.build();
    }
}
