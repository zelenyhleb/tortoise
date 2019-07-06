package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
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
import java.util.Collections;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener {

    private MediaSessionCompat mediaSession;
    private NotificationBuilder notificationBuilder;

    private PlaybackManager playbackManager;
    private TrackProvider trackProvider;

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                switch (intent.getIntExtra("state", -1)) {
                    case Constants.HEADSET_STATE_PLUG_IN:
                        if (getCurrentTrack() != null) {
                            mediaSession.getController().getTransportControls().play();
                        }
                        break;
                    case Constants.HEADSET_STATE_PLUG_OUT:
                        mediaSession.getController().getTransportControls().pause();
                        break;
                }

            }
        }
    };

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent result = new Intent(Constants.ACTION_RESULT_DATA);
            result.putExtra(Constants.EXTRA_POSITION, getProgress());
            result.putExtra(Constants.EXTRA_PLAYBACK_STATE, mediaSession.getController().getPlaybackState());
            result.putExtra(Constants.EXTRA_METADATA, mediaSession.getController().getMetadata());
            sendBroadcast(result);
        }
    };

    private BroadcastReceiver playlistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_SHUFFLE.equals(intent.getAction())){
                playbackManager.shuffleTrackList();
            }
        }
    };

    private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            playbackManager.play();
        }

        @Override
        public void onPause() {
            playbackManager.pause();
        }

        @Override
        public void onSkipToNext() {
            playbackManager.nextTrack();
        }

        @Override
        public void onSkipToPrevious() {
            playbackManager.previousTrack();
        }

        @Override
        public void onSeekTo(long pos) {
            playbackManager.seekTo((int) pos);
        }

        @Override
        public void onSkipToQueueItem(long id) {
            playbackManager.newTrack((int) id);
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
        playbackManager.nextTrack();
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

        playbackManager = new PlaybackManager();
        playbackManager.setPlayerStateCallback(new PlaybackManager.PlayerStateCallback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
                mediaSession.setPlaybackState(stateCompat);
                updateNotification();
            }

            @Override
            public void onTrackChanged(String track) {
                mediaSession.setMetadata(Utils.loadData(track, MediaPlaybackService.this.getContentResolver()).getAsMediaMetadata());
                updateNotification();
            }
        });
        playbackManager.setPlaylistUpdateCallback(new PlaybackManager.PlaylistUpdateCallback() {
            @Override
            public void onPlaylistUpdated(ArrayList<String> list) {
                Intent intent = new Intent(Constants.ACTION_UPDATE_TRACK_LIST);
                intent.putStringArrayListExtra(Constants.EXTRA_TRACK_LIST, list);
                sendBroadcast(intent);
            }
        });

        trackProvider = new TrackProvider(this, new TrackProvider.OnStorageUpdateCallback() {
            @Override
            public void onStorageUpdate() {
                ArrayList<String> storage = trackProvider.getStorage();

                Intent updateIntent = new Intent(Constants.ACTION_UPDATE_STORAGE);
                updateIntent.putExtra(Constants.EXTRA_TRACK_LIST, storage);
                sendBroadcast(updateIntent);

                playbackManager.setTrackList(storage);
            }
        });
        trackProvider.search();

        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, headsetFilter);

        IntentFilter positionFilter = new IntentFilter();
        positionFilter.addAction(Constants.ACTION_REQUEST_DATA);
        registerReceiver(positionReceiver, positionFilter);

        IntentFilter playlistFilter = new IntentFilter();
        playlistFilter.addAction(Constants.ACTION_SHUFFLE);
        registerReceiver(playlistReceiver, playlistFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(headsetReceiver);
        removeNotification();

        super.onDestroy();
    }

    private void updateNotification() {
        notificationBuilder.updateNotification(mediaSession);
    }

    private void removeNotification() {
        notificationBuilder.removeNotification();
    }

    String getCurrentTrack() {
        return playbackManager.getCurrentTrack();
    }

    int getProgress() {
        return playbackManager.getCurrentStreamPosition();
    }

    private MediaMetadataCompat getMetadata() {
        return mediaSession.getController().getMetadata();
    }

    private PlaybackStateCompat getPlaybackState() {
        return mediaSession.getController().getPlaybackState();
    }

}
