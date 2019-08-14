package ru.krivocraft.kbmp;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;
import java.util.Objects;

import ru.krivocraft.kbmp.constants.Constants;

public class MediaPlaybackService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener {

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private NotificationBuilder notificationBuilder;

    private PlaybackManager playbackManager;

    public static boolean running = false;

    private static final int HEADSET_STATE_PLUG_IN = 1;
    private static final int HEADSET_STATE_PLUG_OUT = 0;

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                switch (intent.getIntExtra("state", -1)) {
                    case HEADSET_STATE_PLUG_IN:
                        if (getCurrentTrack() != null) {
                            mediaSession.getController().getTransportControls().play();
                        }
                        break;
                    case HEADSET_STATE_PLUG_OUT:
                        mediaSession.getController().getTransportControls().pause();
                        break;
                }

            }
        }
    };

    private BroadcastReceiver requestDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.Actions.ACTION_REQUEST_DATA.equals(intent.getAction())) {
                Intent result = new Intent(Constants.Actions.ACTION_RESULT_DATA);
                result.putExtra(Constants.Extras.EXTRA_POSITION, getProgress());
                result.putExtra(Constants.Extras.EXTRA_PLAYBACK_STATE, mediaSession.getController().getPlaybackState());
                result.putExtra(Constants.Extras.EXTRA_METADATA, mediaSession.getController().getMetadata());
                sendBroadcast(result);
            } else {
                Intent result = new Intent(Constants.Actions.ACTION_RESULT_TRACK_LIST);
                result.putExtra(Constants.Extras.EXTRA_TRACK_LIST, playbackManager.getTrackList().toJson());
                result.putExtra(Constants.Extras.EXTRA_CURSOR, playbackManager.getCursor());
                sendBroadcast(result);
            }
        }
    };

    private BroadcastReceiver playlistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case Constants.Actions.ACTION_PLAY_FROM_LIST:
                    TrackList trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
                    TrackReference item = TrackReference.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK));
                    if (!trackList.equals(playbackManager.getTrackList())) {
                        playbackManager.setTrackList(trackList, true);
                    }
                    playFromList(item, trackList);
                    break;
                case Constants.Actions.ACTION_REQUEST_STOP:
                    playbackManager.stop();
                    sendBroadcast(new Intent(Constants.Actions.ACTION_HIDE_PLAYER));
                    hideNotification();
                    stopSelf();
                    break;
                case Constants.Actions.ACTION_SHUFFLE:
                    playbackManager.shuffle();
                    break;
                case Constants.Actions.ACTION_EDIT_TRACK_LIST:
                    TrackList in = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
                    TrackReference reference = playbackManager.getCurrentTrack();
                    playbackManager.setTrackList(in, false);
                    playbackManager.setCursor(in.indexOf(reference));
                    break;
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

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        mediaSession.setActive(true);
        mediaSession.setCallback(callback);

        mediaController = mediaSession.getController();

        notificationBuilder = new NotificationBuilder(this);

        playbackManager = new PlaybackManager(this);
        playbackManager.setPlayerStateCallback(new PlaybackManager.PlayerStateCallback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
                mediaSession.setPlaybackState(stateCompat);
                showNotification();
            }

            @Override
            public void onTrackChanged(Track track) {
                mediaSession.setMetadata(track.getAsMediaMetadata());
                showNotification();
            }
        });
        playbackManager.setPlaylistUpdateCallback(this::updateTrackList);

        TrackStorageManager trackStorageManager = new TrackStorageManager(this);
        trackStorageManager.search();

        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, headsetFilter);

        IntentFilter positionFilter = new IntentFilter();
        positionFilter.addAction(Constants.Actions.ACTION_REQUEST_DATA);
        positionFilter.addAction(Constants.Actions.ACTION_REQUEST_TRACK_LIST);
        registerReceiver(requestDataReceiver, positionFilter);

        IntentFilter playlistFilter = new IntentFilter();
        playlistFilter.addAction(Constants.Actions.ACTION_REQUEST_STOP);
        playlistFilter.addAction(Constants.Actions.ACTION_SHUFFLE);
        playlistFilter.addAction(Constants.Actions.ACTION_EDIT_TRACK_LIST);
        playlistFilter.addAction(Constants.Actions.ACTION_PLAY_FROM_LIST);
        registerReceiver(playlistReceiver, playlistFilter);
    }

    private void updateTrackList(TrackList list) {
        Intent intent = new Intent(Constants.Actions.ACTION_UPDATE_TRACK_LIST);
        intent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, list.toJson());
        sendBroadcast(intent);
    }

    private void playFromList(TrackReference track, TrackList trackList) {
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata == null) {
            mediaController.getTransportControls().skipToQueueItem(playbackManager.getTrackList().indexOf(track));
        } else {
            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).equals(Tracks.getTrack(this, track).getPath()) && trackList.equals(playbackManager.getTrackList())) {
                if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
                playbackManager.setCursor(playbackManager.getTrackList().indexOf(track));
            } else {
                mediaController.getTransportControls().skipToQueueItem(playbackManager.getTrackList().indexOf(track));
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        running = true;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        clearShuffleState();
        unregisterReceiver(headsetReceiver);
        unregisterReceiver(playlistReceiver);
        unregisterReceiver(requestDataReceiver);
        running = false;
        super.onDestroy();
    }

    private void showNotification() {
        Notification notification = notificationBuilder.getNotification(mediaSession);
        if (notification != null) {
            startForeground(NotificationBuilder.NOTIFY_ID, notification);
        }
    }

    private void clearShuffleState() {
        SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.SHUFFLE_STATE, Constants.STATE_UNSHUFFLED);
        editor.apply();
    }

    private void hideNotification() {
        stopForeground(true);
    }

    TrackReference getCurrentTrack() {
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
