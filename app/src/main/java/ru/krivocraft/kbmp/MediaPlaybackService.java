package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
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

public class MediaPlaybackService extends MediaBrowserServiceCompat implements StateCallback, MediaPlayer.OnCompletionListener {

    private MediaSessionCompat mediaSession;
    private NotificationBuilder notificationBuilder;

    private PlaybackManager playbackManager;
    private TrackProvider trackProvider;

    private static boolean running = false;

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
        MediaPlaybackService getServerInstance() {
            return MediaPlaybackService.this;
        }

    }

    static boolean isRunning() {
        return running;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSession = new MediaSessionCompat(this, MediaPlaybackService.class.getSimpleName());
        setSessionToken(mediaSession.getSessionToken());

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

        mediaSession.setCallback(callback);

        notificationBuilder = new NotificationBuilder(this);

        playbackManager = new PlaybackManager(new PlaybackManager.PlayerStateCallback() {
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

        trackProvider = new TrackProvider(this, new TrackProvider.OnUpdateCallback() {
            @Override
            public void onUpdate() {
                TrackList trackList = trackProvider.getStorage();

                playbackManager.setTrackList(trackList);
                Intent updateIntent = new Intent(Constants.ACTION_UPDATE_TRACKLIST);
                updateIntent.putExtra("tracklist_extra", trackList);
                sendBroadcast(updateIntent);
            }
        });
        trackProvider.search();

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

    TrackProvider getTrackProvider() {
        return trackProvider;
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
