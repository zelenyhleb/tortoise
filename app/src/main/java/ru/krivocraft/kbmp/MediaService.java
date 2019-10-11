package ru.krivocraft.kbmp;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.util.Objects;

import ru.krivocraft.kbmp.api.TrackListsStorageManager;
import ru.krivocraft.kbmp.api.TracksStorageManager;
import ru.krivocraft.kbmp.constants.Constants;

class MediaService {

    private static final int HEADSET_STATE_PLUG_IN = 1;
    private static final int HEADSET_STATE_PLUG_OUT = 0;

    private final MediaBrowserServiceCompat context;
    private final MediaSessionCompat mediaSession;
    private final MediaControllerCompat mediaController;
    private final NotificationBuilder notificationBuilder;
    private final TracksStorageManager tracksStorageManager;
    private final TrackListsStorageManager trackListsStorageManager;

    private final PlaybackManager playbackManager;

    public MediaService(MediaBrowserServiceCompat context) {
        this.context = context;
        mediaSession = new MediaSessionCompat(context, AndroidMediaService.class.getSimpleName());
        context.setSessionToken(mediaSession.getSessionToken());

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        mediaSession.setActive(true);

        tracksStorageManager = new TracksStorageManager(context);
        trackListsStorageManager = new TrackListsStorageManager(context);

        mediaController = mediaSession.getController();

        notificationBuilder = new NotificationBuilder(context);

        playbackManager = new PlaybackManager(context);
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

        mediaSession.setCallback(new MediaSessionCallback(playbackManager, this::stopPlayback));

        TracksProvider tracksProvider = new TracksProvider(context);
        tracksProvider.search();

        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(headsetReceiver, headsetFilter);

        IntentFilter positionFilter = new IntentFilter();
        positionFilter.addAction(Constants.Actions.ACTION_REQUEST_DATA);
        positionFilter.addAction(Constants.Actions.ACTION_REQUEST_TRACK_LIST);
        context.registerReceiver(requestDataReceiver, positionFilter);

        IntentFilter playlistFilter = new IntentFilter();
        playlistFilter.addAction(Constants.Actions.ACTION_REQUEST_STOP);
        playlistFilter.addAction(Constants.Actions.ACTION_SHUFFLE);
        playlistFilter.addAction(Constants.Actions.ACTION_EDIT_PLAYING_TRACK_LIST);
        playlistFilter.addAction(Constants.Actions.ACTION_EDIT_TRACK_LIST);
        playlistFilter.addAction(Constants.Actions.ACTION_PLAY_FROM_LIST);
        context.registerReceiver(playlistReceiver, playlistFilter);

    }


    private void showNotification() {
        Notification notification = notificationBuilder.getNotification(mediaSession);
        if (notification != null) {
            context.startForeground(NotificationBuilder.NOTIFY_ID, notification);
        }
    }

    private void clearShuffleState() {
        SharedPreferences preferences = context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.SHUFFLE_STATE, Constants.STATE_UNSHUFFLED);
        editor.apply();
    }

    private void hideNotification() {
        context.stopForeground(true);
    }

    private final BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                switch (intent.getIntExtra("state", -1)) {
                    case HEADSET_STATE_PLUG_IN:
                        if (playbackManager.getCurrentTrack() != null) {
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

    private final BroadcastReceiver requestDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.Actions.ACTION_REQUEST_DATA.equals(intent.getAction())) {
                Intent result = new Intent(Constants.Actions.ACTION_RESULT_DATA);
                result.putExtra(Constants.Extras.EXTRA_POSITION, playbackManager.getCurrentStreamPosition());
                result.putExtra(Constants.Extras.EXTRA_PLAYBACK_STATE, mediaSession.getController().getPlaybackState());
                result.putExtra(Constants.Extras.EXTRA_METADATA, mediaSession.getController().getMetadata());
                context.sendBroadcast(result);
            } else {
                Intent result = new Intent(Constants.Actions.ACTION_RESULT_TRACK_LIST);
                result.putExtra(Constants.Extras.EXTRA_TRACK_LIST, playbackManager.getTrackList().toJson());
                result.putExtra(Constants.Extras.EXTRA_CURSOR, playbackManager.getCursor());
                context.sendBroadcast(result);
            }
        }
    };

    private final BroadcastReceiver playlistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case Constants.Actions.ACTION_PLAY_FROM_LIST:
                    playFromList(intent);
                    break;
                case Constants.Actions.ACTION_REQUEST_STOP:
                    stopPlayback();
                    break;
                case Constants.Actions.ACTION_SHUFFLE:
                    shuffle();
                    break;
                case Constants.Actions.ACTION_EDIT_PLAYING_TRACK_LIST:
                    TrackList in = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
                    notifyPlaybackManager(in);
                    break;
                case Constants.Actions.ACTION_EDIT_TRACK_LIST:
                    TrackList trackListEdited = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
                    if (trackListEdited.equals(playbackManager.getTrackList())) {
                        notifyPlaybackManager(trackListEdited);
                    }
                    trackListsStorageManager.updateTrackListData(trackListEdited);
                    context.sendBroadcast(new Intent(Constants.Actions.ACTION_UPDATE_STORAGE));
                    break;
            }
        }
    };

    private void shuffle() {
        playbackManager.shuffle();
    }

    private void stopPlayback() {
        System.out.println(System.currentTimeMillis());
        playbackManager.stop();
        System.out.println(System.currentTimeMillis());
        hideNotification();
        System.out.println(System.currentTimeMillis());
        context.sendBroadcast(new Intent(Constants.Actions.ACTION_HIDE_PLAYER));
        System.out.println(System.currentTimeMillis());
    }

    private void playFromList(Intent intent) {
        TrackList trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
        TrackReference reference = TrackReference.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK));

        if (!trackList.equals(playbackManager.getTrackList())) {
            playbackManager.setTrackList(trackList, true);
        }

        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata == null) {
            mediaController.getTransportControls().skipToQueueItem(playbackManager.getTrackList().indexOf(reference));
        } else {
            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).equals(tracksStorageManager.getTrack(reference).getPath()) && trackList.equals(playbackManager.getTrackList())) {
                if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
                playbackManager.setCursor(playbackManager.getTrackList().indexOf(reference));
            } else {
                mediaController.getTransportControls().skipToQueueItem(playbackManager.getTrackList().indexOf(reference));
            }
        }
    }

    private void updateTrackList(TrackList list) {
        Intent intent = new Intent(Constants.Actions.ACTION_UPDATE_TRACK_LIST);
        intent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, list.toJson());
        context.sendBroadcast(intent);
    }

    private void notifyPlaybackManager(TrackList in) {
        TrackReference reference = playbackManager.getCurrentTrack();
        playbackManager.setTrackList(in, false);
        playbackManager.setCursor(in.indexOf(reference));
    }

    void handleCommand(Intent intent) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
    }

    void destroy() {
        clearShuffleState();

        context.unregisterReceiver(headsetReceiver);
        context.unregisterReceiver(playlistReceiver);
        context.unregisterReceiver(requestDataReceiver);

        playbackManager.getEqualizerManager().destroy();
    }
}
