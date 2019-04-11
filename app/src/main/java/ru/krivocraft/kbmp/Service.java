package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
import java.util.List;

public class Service extends MediaBrowserServiceCompat implements StateCallback, MediaPlayer.OnCompletionListener {

    private MediaSessionCompat mediaSession;
    private NotificationBuilder notificationBuilder;

    private PlaybackManager playbackManager;
    private TrackProvider trackProvider;

    private static boolean running = false;

    private Binder mBinder = new LocalBinder();

    private List<StateCallback> listeners = new ArrayList<>();

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                switch (intent.getIntExtra("state", -1)) {
                    case Constants.HEADSET_STATE_PLUG_IN:
                        if (getCurrentTrack() != null) {
                            play();
                        }
                        break;
                    case Constants.HEADSET_STATE_PLUG_OUT:
                        pause();
                        break;
                }

            }
        }
    };

    private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            play();
        }

        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onSkipToNext() {
            skipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            skipToPrevious();
        }

        @Override
        public void onSeekTo(long pos) {
            seekTo((int) pos);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        skipToNext();
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        updateNotification();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        updateNotification();
    }

    class LocalBinder extends Binder {
        Service getServerInstance() {
            return Service.this;
        }

    }

    static boolean isRunning() {
        return running;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationBuilder = new NotificationBuilder(this);

        mediaSession = new MediaSessionCompat(this, "Tortoise");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

        mediaSession.setCallback(callback);


        playbackManager = new PlaybackManager(this, new PlaybackManager.PlayerStateCallback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
                mediaSession.setPlaybackState(stateCompat);
                mediaSession.setActive(true);
                for (StateCallback listener : listeners) {
                    listener.onPlaybackStateChanged(stateCompat);
                }
            }

            @Override
            public void onTrackChanged(Track track) {
                mediaSession.setMetadata(track.getAsMediaMetadata());
                mediaSession.setActive(true);
                for (StateCallback listener : listeners) {
                    listener.onMetadataChanged(track.getAsMediaMetadata());
                }
            }
        });

        trackProvider = new TrackProvider(this);
        trackProvider.search(new TrackProvider.OnUpdateCallback() {
            @Override
            public void onUpdate() {
                playbackManager.setTrackList(trackProvider.getStorage());
            }
        });

        addStateCallbackListener(this);

        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, headsetFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        running = true;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(headsetReceiver);
        removeNotification();

        running = false;
        super.onDestroy();
    }

    void play() {
        playbackManager.play();
    }

    void pause() {
        playbackManager.pause();
    }

    void seekTo(int progress) {
        playbackManager.seekTo(progress);
    }

    void skipToNew(int trackIndex) {
        playbackManager.newTrack(trackIndex);
    }

    void skipToNext() {
        playbackManager.nextTrack();
    }

    void skipToPrevious() {
        playbackManager.previousTrack();
    }

    private void updateNotification() {
        notificationBuilder.updateNotification(mediaSession);
    }

    private void removeNotification() {
        notificationBuilder.removeNotification();
    }

    Track getCurrentTrack() {
        return playbackManager.getTrackList().getSelectedTrack();
    }

    boolean isPlaying() {
        return playbackManager.isPlaying();
    }

    int getProgress() {
        return playbackManager.getCurrentStreamPosition();
    }

    private void setTrackIndex(int index) {
        playbackManager.getTrackList().setCursor(index);
    }

    TrackList getPlaylist() {
        return playbackManager.getTrackList();
    }

    void setPlaylist(TrackList currentTrackList) {
        playbackManager.setTrackList(currentTrackList);
    }

    void addStateCallbackListener(StateCallback listener) {
        listeners.add(listener);
    }

    void removeStateCallbackListener(StateCallback listener) {
        listeners.remove(listener);
    }

}
