package ru.krivocraft.kbmp;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;

    private MediaControllerCompat.TransportControls transportControls;

    public LargePlayerFragment() {
        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                updateBar();
            }
        };
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
            refreshUI();
            resetBar();
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

    void initControls(Activity context) {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(context);
        this.transportControls = mediaController.getTransportControls();
        mediaController.registerCallback(callback);

        this.metadata = mediaController.getMetadata();
        this.playbackState = mediaController.getPlaybackState();

        requestPosition(context);
    }

    BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackProgress = intent.getIntExtra(Constants.EXTRA_POSITION, 0);
            refreshUI();
        }
    };

    void requestPosition(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_RESULT_DATA);
        context.registerReceiver(positionReceiver, filter);

        Intent intent = new Intent(Constants.ACTION_REQUEST_DATA);
        context.sendBroadcast(intent);
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

        RelativeLayout playerLayout = rootView.findViewById(R.id.layout_player);
        playerLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ImageButton previousTrack = rootView.findViewById(R.id.previous);
        ImageButton nextTrack = rootView.findViewById(R.id.next);

        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transportControls.skipToPrevious();
            }
        });

        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transportControls.skipToNext();
            }
        });

        refreshUI();

        return rootView;
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
            trackImage.setClipToOutline(true);
            trackImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeinshort));
        }

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

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrackPlaying()) {
                    transportControls.pause();
                } else {
                    transportControls.play();
                }
            }
        });

        compositionProgressBar.setMax(duration);
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
    }

}
