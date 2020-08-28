/*
 * Copyright (c) 2020 Nikifor Fedorov
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
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.android.player.views;

import android.animation.LayoutTransition;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.android.BaseFragment;
import ru.krivocraft.tortoise.android.PreferencesManager;
import ru.krivocraft.tortoise.core.api.Control;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.android.player.MediaService;
import ru.krivocraft.tortoise.android.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.android.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.core.utils.Art;
import ru.krivocraft.tortoise.core.utils.Milliseconds;
import ru.krivocraft.tortoise.core.utils.Seconds;
import ru.krivocraft.tortoise.android.thumbnail.Colors;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LargePlayerFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener, PlayerFragment {

    private int tintColor = R.color.green700;
    private View rootView;

    private Timer compositionProgressTimer;

    private TrackList trackList;

    private TracksStorageManager tracksStorageManager;
    private Colors colors;

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;

    private ImageButton shuffle;
    private ImageButton buttonLike;
    private ImageButton loop;
    private Control controller;

    public static LargePlayerFragment newInstance() {
        return new LargePlayerFragment();
    }

    private final BroadcastReceiver trackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
            drawShuffleButton();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context != null) {
            registerTrackListReceiver(context);
        }
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
                    updateBar();
                }
            }, 1000, 1000);
        }
        ImageButton button = rootView.findViewById(R.id.play_pause);
        button.setImageResource(R.drawable.ic_pause);
    }

    private void stopUI() {
        if (compositionProgressTimer != null) {
            compositionProgressTimer.cancel();
            compositionProgressTimer = null;
        }
        ImageButton button = rootView.findViewById(R.id.play_pause);
        button.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        controller.seek(seekBar.getProgress() * 1000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int duration = seekBar.getMax();
        int progress = seekBar.getProgress();
        updateTextViews(duration, progress);
    }

    private void updateTextViews(int duration, int progress) {
        int estimated = duration - progress;
        TextView progressText = rootView.findViewById(R.id.composition_progress);
        progressText.setText(new Seconds(progress).formatted());
        TextView estimatedText = rootView.findViewById(R.id.composition_duration);
        estimatedText.setText(String.format("-%s", new Seconds(estimated).formatted()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Do nothing
    }

    @Override
    public void changeColors(int color) {
        Context context = getContext();
        ImageView trackImage = rootView.findViewById(R.id.track_image);
        SeekBar progressBar = rootView.findViewById(R.id.composition_progress_bar);
        if (context != null && rootView != null) {
            Track track = tracksStorageManager.getTrack(tracksStorageManager.getReference(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));

            progressBar.getProgressDrawable().setColorFilter(colors.getColor(color), PorterDuff.Mode.SRC_ATOP);
            progressBar.getThumb().setColorFilter(colors.getColor(color), PorterDuff.Mode.SRC_ATOP);

            VectorChildFinder finder = new VectorChildFinder(context, R.drawable.ic_track_image_default, trackImage);
            VectorDrawableCompat.VFullPath background = finder.findPathByName("background");


            background.setFillColor(colors.getColor(color));
            drawLikeButton(context, buttonLike, track);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_player_large, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        ImageView trackImage = view.findViewById(R.id.track_image);
        trackImage.setClipToOutline(true);

        buttonLike = view.findViewById(R.id.button_like);

        RelativeLayout playerLayout = view.findViewById(R.id.layout_player);
        playerLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ImageButton previousTrack = view.findViewById(R.id.previous);
        ImageButton nextTrack = view.findViewById(R.id.next);
        loop = view.findViewById(R.id.player_loop);
        shuffle = view.findViewById(R.id.player_shuffle);

        previousTrack.setOnClickListener(v -> controller.previous());
        nextTrack.setOnClickListener(v -> controller.next());
        shuffle.setOnClickListener(v -> shuffle(shuffle));
        loop.setOnClickListener(v -> loop(loop));

        final Context context = getContext();
        if (context != null) {
            this.tracksStorageManager = new TracksStorageManager(context);
            this.colors = new Colors(context);
        }

        showMediaMetadataChanges();
        showPlaybackStateChanges();
    }

    private void drawLikeButton(Context context, ImageButton buttonLike, Track track) {
        if (context != null && track != null) {
            if (track.isLiked()) {
                ImageViewCompat.setImageTintList(buttonLike, ColorStateList.valueOf(ContextCompat.getColor(context, tintColor)));
            } else {
                if (getSettingsManager().get(SettingsStorageManager.KEY_THEME, false)) {
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

    private void swapLikeState(Track track) {
        if (track.isLiked()) {
            track.setLiked(false);
        } else {
            track.setLiked(true);
        }
        tracksStorageManager.updateTrack(track);
    }


    private void updateBar() {
        SeekBar progressBar = rootView.findViewById(R.id.composition_progress_bar);
        int progress = progressBar.getProgress();
        if (progress <= progressBar.getMax()) {
            progressBar.setProgress(progress + 1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getContext()).unregisterReceiver(trackListReceiver);
    }

    public void setInitialData(MediaMetadataCompat metadata, PlaybackStateCompat state, TrackList trackList) {
        setInitialData(metadata, state);
        this.trackList = trackList;
    }

    @Override
    public void setInitialData(MediaMetadataCompat metadata, PlaybackStateCompat state) {
        this.metadata = metadata;
        this.playbackState = state;
    }

    @Override
    public void updateMediaMetadata(MediaMetadataCompat metadata) {
        this.metadata = metadata;
        showMediaMetadataChanges();
    }

    @Override
    public void updatePlaybackState(PlaybackStateCompat state) {
        this.playbackState = state;
        showPlaybackStateChanges();
    }

    @Override
    public void showPlaybackStateChanges() {
        if (rootView != null) {
            int progress = new Milliseconds((int) playbackState.getPosition()).seconds();
            int duration = new Milliseconds((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)).seconds();

            updateTextViews(duration, progress);

            SeekBar progressBar = rootView.findViewById(R.id.composition_progress_bar);
            progressBar.setProgress(progress);

            if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                startUI();
            } else {
                stopUI();
            }

            rootView.findViewById(R.id.play_pause).setOnClickListener(v -> {
                if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    controller.pause();
                } else {
                    controller.play();
                }
            });
        }
    }

    @Override
    public void showMediaMetadataChanges() {
        if (rootView != null) {
            ImageView trackImage = rootView.findViewById(R.id.track_image);
            SeekBar progressBar = rootView.findViewById(R.id.composition_progress_bar);

            Bitmap trackArt = new Art(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)).bitmap();
            Context context = getContext();
            Track track = tracksStorageManager.getTrack(tracksStorageManager.getReference(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));

            if (context != null) {
                if (trackArt != null) {
                    trackImage.setImageBitmap(trackArt);
                } else {
                    int color = track.getColor();
                    tintColor = colors.getColorResource(color);

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        changeColors(color);
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

            TextView title = rootView.findViewById(R.id.composition_name);
            title.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            title.setSelected(true);

            TextView author = rootView.findViewById(R.id.composition_author);
            author.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

            int duration = new Milliseconds((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)).seconds();
            progressBar.setOnSeekBarChangeListener(this);
            progressBar.setMax(duration);
        }
    }

    public void setController(Control controller) {
        this.controller = controller;
    }
}
