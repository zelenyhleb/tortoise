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

package ru.krivocraft.tortoise.fragments;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.ColorManager;
import ru.krivocraft.tortoise.core.playback.MediaService;
import ru.krivocraft.tortoise.core.storage.PreferencesManager;
import ru.krivocraft.tortoise.core.storage.SettingsStorageManager;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;
import ru.krivocraft.tortoise.core.utils.Art;
import ru.krivocraft.tortoise.core.utils.Milliseconds;
import ru.krivocraft.tortoise.core.utils.Seconds;

public class LargePlayerFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {

    private int tintColor = R.color.green700;
    private ImageButton playPauseButton;
    private TextView compositionNameTextView;
    private TextView compositionAuthorTextView;
    private TextView compositionProgressTextView;
    private TextView compositionDurationTextView;
    private SeekBar compositionProgressBar;
    private ImageView trackImage;

    private Handler mHandler;
    private Timer compositionProgressTimer;

    private int trackProgress;
    private TrackList trackList;
    private TrackReference reference;

    private TracksStorageManager tracksStorageManager;
    private ColorManager colorManager;

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;

    private MediaControllerCompat.TransportControls transportControls;
    private ImageButton shuffle;
    private ImageButton buttonLike;
    private ImageButton loop;


    private void initHandler() {
        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                updateBar();
            }
        };
    }

    public static LargePlayerFragment newInstance(Activity activity, TrackList trackList, MediaControllerCompat mediaController) {
        LargePlayerFragment fragment = new LargePlayerFragment();
        fragment.init(activity, trackList, mediaController);
        fragment.initHandler();
        return fragment;
    }

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            LargePlayerFragment.this.playbackState = playbackState;
            trackProgress = (int) playbackState.getPosition();
            updateStateShowers();

        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            LargePlayerFragment.this.metadata = metadata;
            LargePlayerFragment.this.reference = tracksStorageManager.getReference(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
            refreshUI();
            resetBar();
        }
    };

    private BroadcastReceiver trackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
            drawShuffleButton();
        }
    };


    private String getTrackPath() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
    }

    private int getTrackDuration() {
        return (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    private String getTrackArtist() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    private String getTrackTitle() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    private boolean isTrackPlaying() {
        return playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    private void init(Activity context, TrackList trackList, MediaControllerCompat mediaController) {
        this.tracksStorageManager = new TracksStorageManager(context);

        this.transportControls = mediaController.getTransportControls();
        mediaController.registerCallback(callback);

        this.metadata = mediaController.getMetadata();

        String path = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        this.reference = tracksStorageManager.getReference(path);

        this.playbackState = mediaController.getPlaybackState();

        this.trackList = trackList;

        this.colorManager = new ColorManager(context);

        registerTrackListReceiver(context);
        requestPosition(context);
    }

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackProgress = intent.getIntExtra(MediaService.EXTRA_POSITION, 0);
            refreshUI();
        }
    };


    public void requestPosition(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_DATA);
        context.registerReceiver(positionReceiver, filter);

        Intent intent = new Intent(MediaService.ACTION_REQUEST_DATA);
        context.sendBroadcast(intent);
    }

    private void registerTrackListReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_UPDATE_TRACK_LIST);
        context.registerReceiver(trackListReceiver, filter);
    }

    private void startUI() {
        if (compositionProgressTimer == null) {
            compositionProgressTimer = new Timer();
            compositionProgressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(0);
                }
            }, 1000, 1000);
        }
        playPauseButton.setImageResource(R.drawable.ic_pause);
    }

    private void stopUI() {
        if (compositionProgressTimer != null) {
            compositionProgressTimer.cancel();
            compositionProgressTimer = null;
        }
        playPauseButton.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        transportControls.seekTo(seekBar.getProgress() * 1000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int duration = seekBar.getMax();
        int progress = seekBar.getProgress();
        updateTextViews(duration, progress);
    }

    private void updateTextViews(int duration, int progress) {
        int estimated = duration - progress;
        compositionProgressTextView.setText(new Seconds(progress).formatted());
        compositionDurationTextView.setText(String.format("-%s", new Seconds(estimated).formatted()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Do nothing
    }

    @Override
    public void invalidate() {
        refreshUI();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_player_large, container, false);

        playPauseButton = rootView.findViewById(R.id.play_pause);
        compositionNameTextView = rootView.findViewById(R.id.composition_name);
        compositionAuthorTextView = rootView.findViewById(R.id.composition_author);
        compositionProgressTextView = rootView.findViewById(R.id.composition_progress);
        compositionDurationTextView = rootView.findViewById(R.id.composition_duration);
        compositionProgressBar = rootView.findViewById(R.id.composition_progress_bar);
        trackImage = rootView.findViewById(R.id.track_image);
        trackImage.setClipToOutline(true);
        buttonLike = rootView.findViewById(R.id.button_like);

        RelativeLayout playerLayout = rootView.findViewById(R.id.layout_player);
        playerLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ImageButton previousTrack = rootView.findViewById(R.id.previous);
        ImageButton nextTrack = rootView.findViewById(R.id.next);
        loop = rootView.findViewById(R.id.player_loop);
        shuffle = rootView.findViewById(R.id.player_shuffle);

        previousTrack.setOnClickListener(v -> transportControls.skipToPrevious());
        nextTrack.setOnClickListener(v -> transportControls.skipToNext());
        shuffle.setOnClickListener(v -> shuffle(shuffle));
        loop.setOnClickListener(v -> loop(loop));

        refreshUI();

        return rootView;
    }

    private void drawLikeButton(Context context, ImageButton buttonLike, Track track) {
        if (context != null && track != null) {
            if (track.isLiked()) {
                ImageViewCompat.setImageTintList(buttonLike, ColorStateList.valueOf(ContextCompat.getColor(context, tintColor)));
            } else {
                if (getSettingsManager().getOption(SettingsStorageManager.KEY_THEME, false)) {
                    ImageViewCompat.setImageTintList(buttonLike, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)));
                } else {
                    ImageViewCompat.setImageTintList(buttonLike, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
                }
            }
        }
    }

    private void drawLoopButton(ImageButton loop) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences(PreferencesManager.STORAGE_SETTINGS, Context.MODE_PRIVATE);
            int loopState = preferences.getInt(TrackList.LOOP_TYPE, TrackList.LOOP_TRACK_LIST);
            switch (loopState) {
                case TrackList.NOT_LOOP:
                    loop.setImageDrawable(context.getDrawable(R.drawable.ic_loop_not));
                    break;
                case TrackList.LOOP_TRACK:
                    loop.setImageDrawable(context.getDrawable(R.drawable.ic_loop_track));
                    break;
                case TrackList.LOOP_TRACK_LIST:
                    loop.setImageDrawable(context.getDrawable(R.drawable.ic_loop_list));
                    break;
                default:
                    //Do nothing
                    break;
            }
        }
    }

    private void drawShuffleButton() {
        Context context = getContext();
        if (context != null) {
            if (trackList.isShuffled()) {
                shuffle.setImageDrawable(context.getDrawable(R.drawable.ic_shuffled));
            } else {
                shuffle.setImageDrawable(context.getDrawable(R.drawable.ic_unshuffled));
            }
        }
    }

    private void shuffle(ImageButton shuffle) {
        Context context = getContext();
        if (context != null) {
            context.sendBroadcast(new Intent(MediaService.ACTION_SHUFFLE));
            if (trackList.isShuffled()) {
                shuffle.setImageDrawable(context.getDrawable(R.drawable.ic_unshuffled));
            } else {
                shuffle.setImageDrawable(context.getDrawable(R.drawable.ic_shuffled));
            }
        }
    }

    private void loop(ImageView button) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences(PreferencesManager.STORAGE_SETTINGS, Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = preferences.edit();
            int loopState = preferences.getInt(TrackList.LOOP_TYPE, TrackList.LOOP_TRACK_LIST);
            switch (loopState) {
                case TrackList.NOT_LOOP:
                    editor.putInt(TrackList.LOOP_TYPE, TrackList.LOOP_TRACK);
                    button.setImageDrawable(context.getDrawable(R.drawable.ic_loop_track));
                    break;
                case TrackList.LOOP_TRACK:
                    editor.putInt(TrackList.LOOP_TYPE, TrackList.LOOP_TRACK_LIST);
                    button.setImageDrawable(context.getDrawable(R.drawable.ic_loop_list));
                    break;
                case TrackList.LOOP_TRACK_LIST:
                    editor.putInt(TrackList.LOOP_TYPE, TrackList.NOT_LOOP);
                    button.setImageDrawable(context.getDrawable(R.drawable.ic_loop_not));
                    break;
                default:
                    //Do nothing
                    break;
            }
            editor.apply();
        }
    }

    private void refreshUI() {

        Context context = getContext();
        Bitmap trackArt = new Art(getTrackPath()).bitmap();
        Track track = tracksStorageManager.getTrack(reference);
        if (context != null) {
            if (trackArt != null) {
                trackImage.setImageBitmap(trackArt);
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    VectorChildFinder finder = new VectorChildFinder(context, R.drawable.ic_track_image_default, trackImage);
                    VectorDrawableCompat.VFullPath background = finder.findPathByName("background");

                    int color = track.getColor();

                    background.setFillColor(colorManager.getColor(color));
                    tintColor = colorManager.getColorResource(color);
                    drawLikeButton(context, buttonLike, track);

                    compositionProgressBar.getProgressDrawable().setColorFilter(colorManager.getColor(color), PorterDuff.Mode.SRC_ATOP);
                    compositionProgressBar.getThumb().setColorFilter(colorManager.getColor(color), PorterDuff.Mode.SRC_ATOP);
                } else {
                    trackImage.setImageResource(R.drawable.ic_track_image_default);
                }
            }

            buttonLike.setOnClickListener(v -> {
                swapLikeState(track);
                drawLikeButton(context, buttonLike, track);
            });
            drawLikeButton(context, buttonLike, track);
        }

        drawLoopButton(loop);
        drawShuffleButton();

        compositionNameTextView.setText(getTrackTitle());
        compositionNameTextView.setSelected(true);
        compositionAuthorTextView.setText(getTrackArtist());

        int duration = new Milliseconds(getTrackDuration()).seconds();
        compositionProgressBar.setOnSeekBarChangeListener(this);
        compositionProgressBar.setMax(duration);

        updateStateShowers();
    }

    private void updateStateShowers() {
        int progress = new Milliseconds(trackProgress).seconds();
        int duration = new Milliseconds(getTrackDuration()).seconds();

        compositionProgressTextView.setText(new Seconds(progress).formatted());
        compositionDurationTextView.setText(new Seconds((duration - progress) / 1000).formatted());
        compositionProgressBar.setProgress(progress);

        if (isTrackPlaying()) {
            startUI();
        } else {
            stopUI();
        }

        playPauseButton.setOnClickListener(v -> {
            if (isTrackPlaying()) {
                transportControls.pause();
            } else {
                transportControls.play();
            }
        });
    }

    private void swapLikeState(Track track) {
        if (track.isLiked()) {
            track.setLiked(false);
        } else {
            track.setLiked(true);
        }
        tracksStorageManager.updateTrack(track);
    }


    private void updateBar() {
        int progress = compositionProgressBar.getProgress();
        if (progress <= compositionProgressBar.getMax()) {
            compositionProgressBar.setProgress(progress + 1);
        }
    }

    private void resetBar() {
        compositionProgressBar.setProgress(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getContext()).unregisterReceiver(positionReceiver);
        Objects.requireNonNull(getContext()).unregisterReceiver(trackListReceiver);
    }

}
