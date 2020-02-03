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

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;
    private ColorManager colorManager;
    private TracksStorageManager tracksStorageManager;
    private PlayerControlCallback controller;

    private BroadcastReceiver playbackStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playbackState = intent.getParcelableExtra(MediaService.EXTRA_PLAYBACK_STATE);
            showPlaybackStateChanges();
        }
    };

    public void setInitialData(MediaMetadataCompat metadata, PlaybackStateCompat state) {
        this.metadata = metadata;
        this.playbackState = state;
    }

    public void updatePlaybackState(PlaybackStateCompat state) {
        this.playbackState = state;
        showPlaybackStateChanges();
    }

    public void updateMediaMetadata(MediaMetadataCompat metadata) {
        this.metadata = metadata;
        showMetadataChanges();
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
            requestPlaybackState(context);
        }

        //Initial data show when all views were already created
        showMetadataChanges();
        showPlaybackStateChanges();
    }

    private void showPlaybackStateChanges() {
        if (rootView != null) {
            final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
            bar.setProgress(new Milliseconds((int) playbackState.getPosition()).seconds());

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

    private void showMetadataChanges() {
        if (rootView != null) {
            final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
            final TextView viewAuthor = rootView.findViewById(R.id.fragment_composition_author);
            final TextView viewName = rootView.findViewById(R.id.fragment_composition_name);
            final ImageView viewImage = rootView.findViewById(R.id.fragment_track_image);

            bar.setMax(new Milliseconds((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)).seconds());
            viewAuthor.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            viewName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            viewName.setSelected(true);

            final Context context = getContext();
            if (context != null) {
                final View textContainer = rootView.findViewById(R.id.text_container);
                final Bitmap trackArt = new Art(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)).bitmap();

                textContainer.setOnClickListener(v -> context.startActivity(new Intent(context, PlayerActivity.class)));
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
            }
        }
    }

    public void requestPlaybackState(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_DATA);
        context.registerReceiver(playbackStateReceiver, filter);
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
    public void onDestroy() {
        Context context = getContext();
        if (context != null && receiverRegistered) {
            context.unregisterReceiver(playbackStateReceiver);
            receiverRegistered = false;
        }
        super.onDestroy();
    }

    public void setController(PlayerControlCallback controller) {
        this.controller = controller;
    }

}

