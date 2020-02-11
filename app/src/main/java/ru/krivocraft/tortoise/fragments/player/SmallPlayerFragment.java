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

package ru.krivocraft.tortoise.fragments.player;

import android.content.Context;
import android.content.Intent;
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
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.utils.Art;
import ru.krivocraft.tortoise.core.utils.Milliseconds;
import ru.krivocraft.tortoise.fragments.BaseFragment;

import java.util.Timer;
import java.util.TimerTask;

public class SmallPlayerFragment extends BaseFragment implements PlayerFragment {

    private Timer progressBarTimer;
    private View rootView;

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;
    private ColorManager colorManager;
    private TracksStorageManager tracksStorageManager;
    private PlayerController controller;

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
        showMediaMetadataChanges();
    }

    @Override
    public void changeColors(int color) {
        final Context context = getContext();
        if (context != null && rootView != null) {
            final ImageView viewImage = rootView.findViewById(R.id.fragment_track_image);
            final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                VectorChildFinder finder = new VectorChildFinder(context, R.drawable.ic_track_image_default, viewImage);
                VectorDrawableCompat.VFullPath background = finder.findPathByName("background");
                background.setFillColor(color);
                bar.setProgressTintList(ColorStateList.valueOf(color));
            } else {
                viewImage.setImageResource(R.drawable.ic_track_image_default);
            }
        }
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
        rootView.findViewById(R.id.fragment_button_next).setOnClickListener(view1 -> controller.onNext());
        rootView.findViewById(R.id.fragment_button_previous).setOnClickListener(view1 -> controller.onPrevious());

        Context context = getContext();
        if (context != null) {
            this.colorManager = new ColorManager(context);
            this.tracksStorageManager = new TracksStorageManager(context);
        }

        //Initial data show when all views were already created
        showMediaMetadataChanges();
        showPlaybackStateChanges();
    }

    public void showPlaybackStateChanges() {
        if (rootView != null && playbackState != null) {
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

    public void showMediaMetadataChanges() {
        if (rootView != null && metadata != null) {
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
                    int color = colorManager.getColor(tracksStorageManager.getTrack(tracksStorageManager.getReference(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))).getColor());
                    changeColors(color);
                }
                viewImage.setClipToOutline(true);
                viewImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeinshort));
            }
        }
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

    public void setController(PlayerController controller) {
        this.controller = controller;
    }

}


