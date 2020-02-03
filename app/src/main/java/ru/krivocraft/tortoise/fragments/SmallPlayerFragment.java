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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
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
import androidx.annotation.Nullable;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.contexts.PlayerActivity;
import ru.krivocraft.tortoise.core.ColorManager;
import ru.krivocraft.tortoise.core.playback.MediaService;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.utils.Art;
import ru.krivocraft.tortoise.core.utils.Milliseconds;

import java.util.Timer;
import java.util.TimerTask;

public class SmallPlayerFragment extends BaseFragment {

    private Timer progressBarTimer;
    private View rootView;
    private boolean receiverRegistered;

    private int trackProgress;

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;
    private ColorManager colorManager;
    private TracksStorageManager tracksStorageManager;

    private final PlayerUpdater updater = new PlayerUpdater() {
        @Override
        public void onStateChanged(PlaybackStateCompat state) {
            playbackState = state;
            trackProgress = (int) state.getPosition();
            showPlaybackStateChanges();
            invalidate();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            SmallPlayerFragment.this.metadata = metadata;
            invalidate();
        }
    };

    private PlayerControlCallback controller;

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackProgress = intent.getIntExtra(MediaService.EXTRA_POSITION, 0);
            showPlaybackStateChanges();
        }
    };

    public void setInitialData(MediaMetadataCompat metadata, PlaybackStateCompat state) {
        this.metadata = metadata;
        this.playbackState = state;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_player_small, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        Context context = getContext();
        if (context != null) {
            this.colorManager = new ColorManager(context);
            this.tracksStorageManager = new TracksStorageManager(context);
            requestPosition(context);
        }

    }

    public void invalidate() {
        final Context context = getContext();

        final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
        final TextView viewAuthor = rootView.findViewById(R.id.fragment_composition_author);
        final TextView viewName = rootView.findViewById(R.id.fragment_composition_name);

        viewAuthor.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        viewName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        viewName.setSelected(true);

        Bitmap trackArt = new Art(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)).bitmap();

        if (context != null) {
            final ImageView viewImage = rootView.findViewById(R.id.fragment_track_image);
            final View textContainer = rootView.findViewById(R.id.text_container);
            if (trackArt != null) {
                viewImage.setImageBitmap(trackArt);
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    VectorChildFinder finder = new VectorChildFinder(context, R.drawable.ic_track_image_default, viewImage);
                    VectorDrawableCompat.VFullPath background = finder.findPathByName("background");
                    int color = colorManager.getColor(tracksStorageManager.getTrack(tracksStorageManager.getReference(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))).getColor());
                    background.setFillColor(color);
                    bar.setProgressTintList(ColorStateList.valueOf(color));
                } else {
                    viewImage.setImageResource(R.drawable.ic_track_image_default);
                }

            }
            viewImage.setClipToOutline(true);
            viewImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeinshort));

            textContainer.setOnClickListener(v -> context.startActivity(new Intent(context, PlayerActivity.class)));
        }

        ImageButton previousCompositionButton = rootView.findViewById(R.id.fragment_button_previous);
        ImageButton nextCompositionButton = rootView.findViewById(R.id.fragment_button_next);

        previousCompositionButton.setOnClickListener(v -> controller.onPrevious());
        nextCompositionButton.setOnClickListener(v -> controller.onNext());

        bar.setMax(new Milliseconds((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)).seconds());
        bar.setProgress(new Milliseconds(trackProgress).seconds());

        showPlaybackStateChanges();
    }

    private void showPlaybackStateChanges() {
        if (rootView != null) {
            final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
            bar.setProgress(new Milliseconds(trackProgress).seconds());

            ImageButton playPauseCompositionButton = rootView.findViewById(R.id.fragment_button_playpause);
            if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                playPauseCompositionButton.setImageResource(R.drawable.ic_pause);
                playPauseCompositionButton.setOnClickListener(v -> controller.onPause());
                cancelCurrentTimer();
                startNewTimer(bar);
            } else {
                playPauseCompositionButton.setImageResource(R.drawable.ic_play);
                playPauseCompositionButton.setOnClickListener(v -> controller.onPlay());
                cancelCurrentTimer();
            }
        }

    }

    public void requestPosition(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_DATA);
        context.registerReceiver(positionReceiver, filter);
        receiverRegistered = true;

        Intent intent = new Intent(MediaService.ACTION_REQUEST_DATA);
        context.sendBroadcast(intent);
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
    public void onDetach() {
        super.onDetach();

        Context context = getContext();
        if (context != null && receiverRegistered) {
            context.unregisterReceiver(positionReceiver);
            receiverRegistered = false;
        }
    }

    public PlayerUpdater getUpdater() {
        return updater;
    }

    public void setController(PlayerControlCallback controller) {
        this.controller = controller;
    }

}


