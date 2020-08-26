package ru.krivocraft.tortoise.core.base;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import ru.krivocraft.tortoise.core.api.AudioFocus;

public class AndroidAudioFocus implements AudioFocus {

    private final AudioManager manager;
    private final AudioFocusListener androidListener;

    public AndroidAudioFocus(ChangeListener listener, AudioManager manager) {
        this.manager = manager;
        this.androidListener = new AudioFocusListener(listener);
    }

    @Override
    public void request() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(androidListener)
                    .build();

            manager.requestAudioFocus(focusRequest);
        } else {
            manager.requestAudioFocus(androidListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    @Override
    public void release() {
        manager.abandonAudioFocus(androidListener);
    }

    private static final class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {

        private final AudioFocus.ChangeListener listener;

        private AudioFocusListener(ChangeListener listener) {
            this.listener = listener;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    listener.mute();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    listener.silently();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    listener.gain();
                    break;
                default:
                    //Do nothing
                    break;
            }
        }
    }
}
