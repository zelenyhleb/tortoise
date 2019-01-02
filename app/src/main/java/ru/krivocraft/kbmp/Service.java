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

public class Service extends MediaBrowserServiceCompat implements Track.StateCallback, MediaPlayer.OnCompletionListener {

    private MediaSessionCompat mediaSession;
    private NotificationBuilder notificationBuilder;

    private PlaybackManager playbackManager;
    private static boolean running = false;

    private Binder mBinder = new LocalBinder();

    private List<Track.StateCallback> listeners = new ArrayList<>();

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

        playbackManager = new PlaybackManager(this, new PlaybackManager.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
                mediaSession.setPlaybackState(stateCompat);
                mediaSession.setActive(true);
                for (Track.StateCallback listener : listeners) {
                    listener.onPlaybackStateChanged(stateCompat);
                }
            }

            @Override
            public void onTrackChanged(Track track) {
                mediaSession.setMetadata(track.getAsMediaMetadata());
                mediaSession.setActive(true);
                for (Track.StateCallback listener : listeners) {
                    listener.onMetadataChanged(track.getAsMediaMetadata());
                }
            }
        });

        IntentFilter musicFilter = new IntentFilter();
        musicFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, musicFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addListener(this);

        MediaButtonReceiver.handleIntent(mediaSession, intent);

        running = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        sendBroadcast(new Intent().setAction(Constants.ACTION_PREVIOUS));
        unregisterReceiver(headsetReceiver);
        removeNotification();
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
        if (getCurrentTrack() != null)
            notificationBuilder.updateNotification(mediaSession);
    }

    private void removeNotification() {
        notificationBuilder.removeNotification();
    }

    Track getCurrentTrack() {
        return playbackManager.getPlaylist().getSelectedTrack();
    }

    boolean isPlaying() {
        return playbackManager.isPlaying();
    }

    int getProgress() {
        return playbackManager.getProgress();
    }

    private void setTrackIndex(int index) {
        playbackManager.getPlaylist().setCursor(index);
    }

    Playlist getPlaylist() {
        return playbackManager.getPlaylist();
    }

    void setPlaylist(Playlist currentPlaylist) {
        playbackManager.setPlaylist(currentPlaylist);
    }

    void addListener(Track.StateCallback listener) {
        listeners.add(listener);
    }

    void removeListener(Track.StateCallback listener) {
        listeners.remove(listener);
    }

}
