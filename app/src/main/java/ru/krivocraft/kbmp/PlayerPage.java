package ru.krivocraft.kbmp;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerPage extends Fragment implements SeekBar.OnSeekBarChangeListener, Track.StateCallback {

    private TortoiseService serviceInstance;
    private Context context;
    private ImageButton playPauseButton;
    private TextView compositionNameTextView;
    private TextView compositionAuthorTextView;
    private TextView compositionProgressTextView;
    private TextView compositionDurationTextView;
    private SeekBar compositionProgressBar;
    private ImageView trackImage;
    private Handler mHandler;

    public PlayerPage() {
        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                updateBar();
            }
        };
    }

    void setServiceInstance(TortoiseService serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    void setContext(Context context) {
        this.context = context;
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
        serviceInstance.seekTo(seekBar.getProgress() * 1000);
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
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

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
                serviceInstance.skipToPrevious();
            }
        });

        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceInstance.skipToNext();
            }
        });

        updateUI();

        return rootView;
    }

    private void updateUI() {
        Track currentTrack = serviceInstance.getCurrentTrack();

        if (currentTrack != null) {

            int progress = Utils.getSeconds(serviceInstance.getProgress());

            String compositionName = currentTrack.getName();
            String compositionComposer = currentTrack.getArtist();
            String compositionDuration = currentTrack.getDuration();

            compositionProgressTextView.setText(Utils.getFormattedTime(progress));
            compositionDurationTextView.setText(Utils.getFormattedTime((Integer.parseInt(compositionDuration) - progress) / 1000));

            final Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fadein);

            Track.GetBitmapTask task = new Track.GetBitmapTask();
            task.setListener(new Track.OnPictureProcessedListener() {
                @Override
                public void onPictureProcessed(final Bitmap bitmap) {
                    if (bitmap != null) {
                        trackImage.setImageBitmap(bitmap);
                    } else {
                        trackImage.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));
                    }
                    trackImage.startAnimation(fadeIn);
                }
            });
            task.execute(new File(currentTrack.getPath()));

            compositionProgressBar.setProgress(progress);
            compositionProgressBar.setOnSeekBarChangeListener(this);

            compositionNameTextView.setText(compositionName);
            compositionNameTextView.setSelected(true);
            compositionAuthorTextView.setText(compositionComposer);

            if (serviceInstance.isPlaying()) {
                startUI();
            } else {
                stopUI();
            }

            playPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (serviceInstance.isPlaying()) {
                        serviceInstance.pause();
                    } else {
                        serviceInstance.play();
                    }
                }
            });

            compositionProgressBar.setMax(Integer.parseInt(compositionDuration) / 1000);
        }

    }

    private void updateBar() {
        int progressMillis = serviceInstance.getProgress();
        int progress = Utils.getSeconds(progressMillis);
        if (progress <= compositionProgressBar.getMax()) {
            compositionProgressBar.setProgress(progress);
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        updateUI();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            startUI();
        } else {
            stopUI();
        }
    }
}
