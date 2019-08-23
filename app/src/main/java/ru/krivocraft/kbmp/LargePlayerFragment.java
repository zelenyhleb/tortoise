package ru.krivocraft.kbmp;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.widget.ImageViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ru.krivocraft.kbmp.constants.Constants;

public class LargePlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private ImageButton playPauseButton;
    private TextView compositionNameTextView;
    private TextView compositionAuthorTextView;
    private TextView compositionProgressTextView;
    private TextView compositionDurationTextView;
    private SeekBar compositionProgressBar;
    private ImageView trackImage;
    private Handler mHandler;

    private int trackProgress;
    private TrackList trackList;
    private TrackReference reference;

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;

    private MediaControllerCompat.TransportControls transportControls;
    private ImageButton shuffle;
    private ImageButton buttonLike;
    private ImageButton loop;

    public LargePlayerFragment() {
        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                updateBar();
            }
        };
    }

    static LargePlayerFragment newInstance(Activity activity, TrackList trackList) {
        LargePlayerFragment fragment = new LargePlayerFragment();
        fragment.init(activity, trackList);
        return fragment;
    }

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            LargePlayerFragment.this.playbackState = playbackState;
            trackProgress = (int) playbackState.getPosition();
            refreshUI();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            LargePlayerFragment.this.metadata = metadata;
            Context context = getContext();
            if (context != null) {
                LargePlayerFragment.this.reference = Tracks.getReference(context, metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
            }
            refreshUI();
            resetBar();
        }
    };

    private BroadcastReceiver trackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
            drawShuffleButton();
        }
    };

    public String getTrackPath() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
    }

    public int getTrackDuration() {
        return (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    public String getTrackArtist() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    public String getTrackTitle() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    public boolean isTrackPlaying() {
        return playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    private void init(Activity context, TrackList trackList) {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(context);
        this.transportControls = mediaController.getTransportControls();
        mediaController.registerCallback(callback);

        this.metadata = mediaController.getMetadata();

        String path = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        this.reference = Tracks.getReference(context, path);

        this.playbackState = mediaController.getPlaybackState();

        this.trackList = trackList;

        registerTrackListReceiver(context);
        requestPosition(context);
    }

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackProgress = intent.getIntExtra(Constants.Extras.EXTRA_POSITION, 0);
            refreshUI();
        }
    };

    void requestPosition(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Actions.ACTION_RESULT_DATA);
        context.registerReceiver(positionReceiver, filter);

        Intent intent = new Intent(Constants.Actions.ACTION_REQUEST_DATA);
        context.sendBroadcast(intent);
    }

    void registerTrackListReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Actions.ACTION_UPDATE_TRACK_LIST);
        context.registerReceiver(trackListReceiver, filter);
    }

    private Timer compositionProgressTimer;

    private void startUI() {
        if (compositionProgressTimer == null) {
            compositionProgressTimer = new Timer();
            compositionProgressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(0);
                }
            }, Constants.ONE_SECOND, Constants.ONE_SECOND);
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
        compositionProgressTextView.setText(Utils.getFormattedTime(progress));
        compositionDurationTextView.setText(String.format("-%s", Utils.getFormattedTime(estimated)));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        if (context != null) {
            if (track != null) {
                if (track.isLiked()) {
                    ImageViewCompat.setImageTintList(buttonLike, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green700)));
                } else {
                    if (Utils.getOption(context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE), Constants.KEY_THEME, false)){
                        ImageViewCompat.setImageTintList(buttonLike, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)));
                    } else {
                        ImageViewCompat.setImageTintList(buttonLike, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
                    }
                }
            }
        }
    }

    private void drawLoopButton(ImageButton loop) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE);
            int loopState = preferences.getInt(Constants.LOOP_TYPE, Constants.NOT_LOOP);
            switch (loopState) {
                case Constants.NOT_LOOP:
                    loop.setImageDrawable(context.getDrawable(R.drawable.ic_loop_not));
                    break;
                case Constants.LOOP_TRACK:
                    loop.setImageDrawable(context.getDrawable(R.drawable.ic_loop_track));
                    break;
                case Constants.LOOP_TRACK_LIST:
                    loop.setImageDrawable(context.getDrawable(R.drawable.ic_loop_list));
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
            context.sendBroadcast(new Intent(Constants.Actions.ACTION_SHUFFLE));
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
            SharedPreferences preferences = context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            int loopState = preferences.getInt(Constants.LOOP_TYPE, Constants.NOT_LOOP);
            switch (loopState) {
                case Constants.NOT_LOOP:
                    editor.putInt(Constants.LOOP_TYPE, Constants.LOOP_TRACK);
                    button.setImageDrawable(context.getDrawable(R.drawable.ic_loop_track));
                    break;
                case Constants.LOOP_TRACK:
                    editor.putInt(Constants.LOOP_TYPE, Constants.LOOP_TRACK_LIST);
                    button.setImageDrawable(context.getDrawable(R.drawable.ic_loop_list));
                    break;
                case Constants.LOOP_TRACK_LIST:
                    editor.putInt(Constants.LOOP_TYPE, Constants.NOT_LOOP);
                    button.setImageDrawable(context.getDrawable(R.drawable.ic_loop_not));
                    break;
            }
            editor.apply();
        }
    }

    private void refreshUI() {
        int progress = Utils.getSeconds(trackProgress);
        int duration = Utils.getSeconds(getTrackDuration());

        compositionProgressTextView.setText(Utils.getFormattedTime(progress));
        compositionDurationTextView.setText(Utils.getFormattedTime((duration - progress) / 1000));

        Context context = getContext();
        Bitmap trackArt = Utils.loadArt(getTrackPath());
        if (context != null) {
            if (trackArt != null) {
                trackImage.setImageBitmap(trackArt);
            } else {
                trackImage.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));
            }
            trackImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeinshort));

            Track track = Tracks.getTrack(context, reference);

            buttonLike.setOnClickListener(v -> {
                swapLikeState(context, track);
                drawLikeButton(context, buttonLike, track);
            });
            drawLikeButton(context, buttonLike, track);
        }

        drawLoopButton(loop);
        drawShuffleButton();

        compositionProgressBar.setProgress(progress);
        compositionProgressBar.setOnSeekBarChangeListener(this);

        compositionNameTextView.setText(getTrackTitle());
        compositionNameTextView.setSelected(true);
        compositionAuthorTextView.setText(getTrackArtist());

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

        compositionProgressBar.setMax(duration);
    }

    private void swapLikeState(Context context, Track track) {
        if (track.isLiked()) {
            track.setLiked(false);
        } else {
            track.setLiked(true);
        }
        Tracks.updateTrack(context, reference, track);
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
