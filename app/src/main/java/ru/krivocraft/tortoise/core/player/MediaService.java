/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.core.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import ru.krivocraft.tortoise.core.MainActivity;
import ru.krivocraft.tortoise.core.explorer.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.rating.Rating;
import ru.krivocraft.tortoise.core.rating.RatingImpl;
import ru.krivocraft.tortoise.core.tracklist.TracksProvider;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.util.Objects;

import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;

public class MediaService {


    private static final String EXTRA_CURSOR = "cursor";
    private static final String EXTRA_METADATA = "metadata";

    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_PLAYBACK_STATE = "playback_state";
    public static final String ACTION_UPDATE_TRACK_LIST = "action_update_track_list";
    public static final String ACTION_REQUEST_TRACK_LIST = "action_request_track_list";

    public static final String ACTION_RESULT_TRACK_LIST = "result_track_list";
    public static final String ACTION_RESULT_DATA = "result_position";
    public static final String ACTION_REQUEST_STOP = "stop";
    public static final String ACTION_REQUEST_DATA = "request_position";
    public static final String ACTION_EDIT_TRACK_LIST = "edit_track_list";
    public static final String ACTION_PLAY_FROM_LIST = "play_from_list";
    public static final String ACTION_EDIT_PLAYING_TRACK_LIST = "edit_current_track_list";
    public static final String ACTION_SHUFFLE = "shuffle";
    public static final String ACTION_NEXT_TRACK = "nextTrack";


    private static final int HEADSET_STATE_PLUG_IN = 1;
    private static final int HEADSET_STATE_PLUG_OUT = 0;

    private final MediaBrowserServiceCompat context;
    private final MediaSessionCompat mediaSession;
    private final MediaControllerCompat mediaController;
    private final NotificationManager notificationManager;
    private final TracksStorageManager tracksStorageManager;
    private final TrackListsStorageManager trackListsStorageManager;
    private final Rating rating;

    private final PlaybackManager playback;

    public MediaService(MediaBrowserServiceCompat context) {
        this.context = context;
        notificationManager = new NotificationManager(context);
        tracksStorageManager = new TracksStorageManager(context);
        trackListsStorageManager = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL);
        rating = new RatingImpl(tracksStorageManager);

        mediaSession = new MediaSessionCompat(context, PlaybackManager.class.getSimpleName());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        mediaSession.setActive(true);

        playback = new PlaybackManager(context,
                new PlayerStateCallback(this::onPlaybackStateChanged, this::onTrackChanged),
                this::updateTrackList);

        mediaSession.setCallback(new MediaSessionCallback(playback, playback::stop, rating));

        context.setSessionToken(mediaSession.getSessionToken());
        mediaController = mediaSession.getController();

        TracksProvider tracksProvider = new TracksProvider(context);
        tracksProvider.search();


        initReceivers();
    }

    private void onTrackChanged(Track track) {
        mediaSession.setMetadata(track.getAsMediaMetadata());
        showNotification();
    }

    private void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
        mediaSession.setPlaybackState(stateCompat);
        showNotification();

        if (stateCompat.getState() == PlaybackStateCompat.STATE_STOPPED) {
            onStop();
        }
    }

    private void onStop() {
        hideNotification();
        context.sendBroadcast(new Intent(MainActivity.ACTION_HIDE_PLAYER));
    }

    private void initReceivers() {
        registerReceiver(headsetReceiver, Intent.ACTION_HEADSET_PLUG, ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(requestDataReceiver, ACTION_REQUEST_DATA, ACTION_REQUEST_TRACK_LIST);
        registerReceiver(playlistReceiver, ACTION_REQUEST_STOP, ACTION_SHUFFLE,
                ACTION_EDIT_PLAYING_TRACK_LIST, ACTION_EDIT_TRACK_LIST,
                ACTION_PLAY_FROM_LIST, ACTION_NEXT_TRACK);
        registerReceiver(colorRequestReceiver, Colors.ACTION_REQUEST_COLOR);
    }

    private void registerReceiver(BroadcastReceiver receiver, String... actions) {
        IntentFilter filter = new IntentFilter();
        for (String action : actions) {
            filter.addAction(action);
        }
        context.registerReceiver(receiver, filter);
    }


    private void showNotification() {
        notificationManager.showNotification(mediaSession);
    }

    private void hideNotification() {
        notificationManager.dismissNotification();
    }

    private final BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                //Old method works only with wired headset and some bluetooth headphones.
                //The feature is replaying audio when user plugs headphones back
                switch (intent.getIntExtra("state", -1)) {
                    case HEADSET_STATE_PLUG_IN:
                        if (playback.getSelectedTrackReference() != null) {
                            mediaSession.getController().getTransportControls().play();
                        }
                        break;
                    case HEADSET_STATE_PLUG_OUT:
                        mediaSession.getController().getTransportControls().pause();
                        break;
                    default:
                        //No idea what to do in this case
                        break;
                }
            } else {
                //Mute if some device unplugged
                mediaSession.getController().getTransportControls().pause();
            }
        }
    };


    private final BroadcastReceiver requestDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_REQUEST_DATA.equals(intent.getAction())) {
                Intent result = new Intent(ACTION_RESULT_DATA);
                result.putExtra(EXTRA_POSITION, playback.getCurrentStreamPosition());
                result.putExtra(EXTRA_PLAYBACK_STATE, mediaSession.getController().getPlaybackState());
                result.putExtra(EXTRA_METADATA, mediaSession.getController().getMetadata());
                context.sendBroadcast(result);
            } else {
                Intent result = new Intent(ACTION_RESULT_TRACK_LIST);
                result.putExtra(TrackList.EXTRA_TRACK_LIST, playback.getTrackList().toJson());
                result.putExtra(Track.EXTRA_TRACK, playback.getSelectedTrackReference().toJson());
                result.putExtra(EXTRA_CURSOR, playback.getCursor());
                context.sendBroadcast(result);
            }
        }
    };


    private final BroadcastReceiver playlistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_PLAY_FROM_LIST:
                    playFromList(intent);
                    break;
                case ACTION_REQUEST_STOP:
                    playback.stop();
                    break;
                case ACTION_SHUFFLE:
                    shuffle();
                    break;
                case ACTION_EDIT_PLAYING_TRACK_LIST:
                    TrackList in = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
                    notifyPlaybackManager(in);
                    break;
                case ACTION_EDIT_TRACK_LIST:
                    TrackList trackListEdited = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
                    if (trackListEdited.equals(playback.getTrackList())) {
                        notifyPlaybackManager(trackListEdited);
                    }
                    trackListsStorageManager.updateTrackListContent(trackListEdited);
                    context.sendBroadcast(new Intent(TracksProvider.ACTION_UPDATE_STORAGE));
                    break;
                case ACTION_NEXT_TRACK:
                    playback.proceed();
                    break;
                default:
                    //Do nothing
                    break;
            }
        }
    };

    private void shuffle() {
        playback.shuffle();
    }

    private void playFromList(Intent intent) {
        TrackList trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
        TrackReference reference = TrackReference.fromJson(intent.getStringExtra(Track.EXTRA_TRACK));

        rating.rate(reference, 1);

        if (!trackList.equals(playback.getTrackList())) {
            playback.setTrackList(trackList, true);
        }

        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata == null) {
            mediaController.getTransportControls().skipToQueueItem(playback.getTrackList().indexOf(reference));
        } else {
            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).equals(tracksStorageManager.getTrack(reference).getPath()) && trackList.equals(playback.getTrackList())) {
                if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
                playback.setCursor(playback.getTrackList().indexOf(reference));
            } else {
                mediaController.getTransportControls().skipToQueueItem(playback.getTrackList().indexOf(reference));
            }
        }
    }

    private final BroadcastReceiver colorRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackReference currentTrack = playback.getSelectedTrackReference();
            int color = -1;
            if (currentTrack != null) {
                Track track = tracksStorageManager.getTrack(currentTrack);
                color = track.getColor();
            }
            context.sendBroadcast(new Intent(Colors.ACTION_RESULT_COLOR)
                    .putExtra(Colors.EXTRA_COLOR, color));

        }
    };

    private void updateTrackList(TrackList list) {
        Intent intent = new Intent(ACTION_UPDATE_TRACK_LIST);
        intent.putExtra(TrackList.EXTRA_TRACK_LIST, list.toJson());
        context.sendBroadcast(intent);
    }

    private void notifyPlaybackManager(TrackList in) {
        TrackReference reference = playback.getSelectedTrackReference();
        playback.setTrackList(in, false);
        playback.setCursor(in.indexOf(reference));
    }

    public void handleCommand(Intent intent) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
    }

    public void destroy() {
        hideNotification();

        context.unregisterReceiver(headsetReceiver);
        context.unregisterReceiver(playlistReceiver);
        context.unregisterReceiver(requestDataReceiver);
        context.unregisterReceiver(colorRequestReceiver);

        playback.destroy();
    }
}
