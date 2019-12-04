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

package ru.krivocraft.kbmp.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.contexts.PlayerActivity;
import ru.krivocraft.kbmp.core.ColorManager;
import ru.krivocraft.kbmp.core.playback.MediaService;
import ru.krivocraft.kbmp.core.storage.TracksStorageManager;
import ru.krivocraft.kbmp.core.utils.Art;
import ru.krivocraft.kbmp.core.utils.Milliseconds;

public class SmallPlayerFragment extends BaseFragment {

    private Timer progressBarTimer;
    private View rootView;
    private MediaControllerCompat.TransportControls transportControls;

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;
    private ColorManager colorManager;
    private TracksStorageManager tracksStorageManager;

    private int trackProgress;

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            playbackState = state;
            trackProgress = (int) state.getPosition();
            refreshStateShowers();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            SmallPlayerFragment.this.metadata = metadata;
            invalidate();
        }
    };

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackProgress = intent.getIntExtra(MediaService.EXTRA_POSITION, 0);
            refreshStateShowers();
        }
    };

    public void init(Activity context, MediaControllerCompat mediaController) {
        this.transportControls = mediaController.getTransportControls();

        mediaController.registerCallback(callback);

        this.metadata = mediaController.getMetadata();
        this.playbackState = mediaController.getPlaybackState();
        this.colorManager = new ColorManager(context);
        this.tracksStorageManager = new TracksStorageManager(context);

        requestPosition(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_player_small, container, false);
        invalidate();
        return rootView;
    }

    public void invalidate() {
        final Context context = getContext();

        final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
        final TextView viewAuthor = rootView.findViewById(R.id.fragment_composition_author);
        final TextView viewName = rootView.findViewById(R.id.fragment_composition_name);
        final ImageView viewImage = rootView.findViewById(R.id.fragment_track_image);

        viewAuthor.setText(getTrackArtist());
        viewName.setText(getTrackTitle());
        viewName.setSelected(true);

        Bitmap trackArt = new Art(getTrackPath()).bitmap();

        if (context != null) {
            rootView.findViewById(R.id.text_container).setOnClickListener(v -> context.startActivity(new Intent(context, PlayerActivity.class)));
            if (trackArt != null) {
                viewImage.setImageBitmap(trackArt);
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    VectorChildFinder finder = new VectorChildFinder(context, R.drawable.ic_track_image_default, viewImage);
                    VectorDrawableCompat.VFullPath background = finder.findPathByName("background");
                    int color = colorManager.getColor(tracksStorageManager.getTrack(tracksStorageManager.getReference(getTrackPath())).getColor());
                    background.setFillColor(color);
                    bar.setProgressTintList(ColorStateList.valueOf(color));
                } else {
                    viewImage.setImageResource(R.drawable.ic_track_image_default);
                }

            }
            viewImage.setClipToOutline(true);
            viewImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeinshort));
        }

        ImageButton previousCompositionButton = rootView.findViewById(R.id.fragment_button_previous);
        ImageButton nextCompositionButton = rootView.findViewById(R.id.fragment_button_next);

        previousCompositionButton.setOnClickListener(v -> transportControls.skipToPrevious());
        nextCompositionButton.setOnClickListener(v -> transportControls.skipToNext());

        bar.setMax(new Milliseconds(getTrackDuration()).seconds());
        bar.setProgress(new Milliseconds(trackProgress).seconds());

        refreshStateShowers();
    }

    private void refreshStateShowers() {
        if (rootView != null) {
            final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
            bar.setProgress(new Milliseconds(trackProgress).seconds());

            ImageButton playPauseCompositionButton = rootView.findViewById(R.id.fragment_button_playpause);
            if (isTrackPlaying()) {
                playPauseCompositionButton.setImageResource(R.drawable.ic_pause);
                playPauseCompositionButton.setOnClickListener(v -> transportControls.pause());
                cancelCurrentTimer();
                startNewTimer(bar);
            } else {
                playPauseCompositionButton.setImageResource(R.drawable.ic_play);
                playPauseCompositionButton.setOnClickListener(v -> transportControls.play());
                cancelCurrentTimer();
            }
        }

    }

    public void requestPosition(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_DATA);
        context.registerReceiver(positionReceiver, filter);

        Intent intent = new Intent(MediaService.ACTION_REQUEST_DATA);
        context.sendBroadcast(intent);
    }

    private String getTrackTitle() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    private String getTrackArtist() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    private int getTrackDuration() {
        return (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    private String getTrackPath() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
    }

    private boolean isTrackPlaying() {
        return playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    private void startNewTimer(final ProgressBar bar) {
        progressBarTimer = new Timer();
        progressBarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                bar.setProgress(bar.getProgress() + 1);
            }
        }, 1000, 1000);
    }

    private void cancelCurrentTimer() {
        if (progressBarTimer != null) {
            progressBarTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getContext()).unregisterReceiver(positionReceiver);
    }
}
