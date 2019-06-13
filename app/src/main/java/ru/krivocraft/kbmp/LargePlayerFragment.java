package ru.krivocraft.kbmp;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class LargePlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, StateCallback {

    private ImageButton playPauseButton;
    private TextView compositionNameTextView;
    private TextView compositionAuthorTextView;
    private TextView compositionProgressTextView;
    private TextView compositionDurationTextView;
    private SeekBar compositionProgressBar;
    private ImageView trackImage;
    private Handler mHandler;

    private String trackArtist;
    private String trackTitle;
    private int trackDuration;
    private int trackProgress;
    private boolean trackIsPlaying;
    private MediaControllerCompat.TransportControls transportControls;
    private SharedPreferences cache;
    private MediaControllerCompat mediaController;

    public LargePlayerFragment() {
        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                updateBar();
            }
        };
    }

    void initControls(Activity context) {
        this.cache = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
        this.mediaController = MediaControllerCompat.getMediaController(context);
        this.transportControls = mediaController.getTransportControls();
        getInitialMetadata();
    }

    private void getInitialMetadata() {
        MediaMetadataCompat metadata = mediaController.getMetadata();
        this.trackArtist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        this.trackTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        this.trackDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        this.trackProgress = (int) mediaController.getPlaybackState().getBufferedPosition();
        this.trackIsPlaying = mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
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

        updateUI();

        return rootView;
    }

    private void updateUI() {
//        Track currentTrack = serviceInstance.getCurrentTrack();
        int progress = Utils.getSeconds(trackProgress);
        int duration = Utils.getSeconds(trackDuration);

        compositionProgressTextView.setText(Utils.getFormattedTime(progress));
        compositionDurationTextView.setText(Utils.getFormattedTime((duration - progress) / 1000));

//        final Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fadein);
//
//        GetBitmapTask task = new GetBitmapTask();
//        task.setListener(new OnPictureProcessedListener() {
//            @Override
//            public void onPictureProcessed(final Bitmap bitmap) {
//                if (bitmap != null) {
//                    trackImage.setImageBitmap(bitmap);
//                } else {
//                    trackImage.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));
//                }
//                trackImage.startAnimation(fadeIn);
//            }
//        });
//        task.execute(new File(currentTrack.getPath()));

        compositionProgressBar.setProgress(progress);
        compositionProgressBar.setOnSeekBarChangeListener(this);

        compositionNameTextView.setText(trackTitle);
        compositionNameTextView.setSelected(true);
        compositionAuthorTextView.setText(trackArtist);

        if (trackIsPlaying) {
            startUI();
        } else {
            stopUI();
        }

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackIsPlaying) {
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
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        trackArtist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        trackTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        trackDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        updateUI();
        resetBar();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        trackIsPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        trackProgress = (int) playbackState.getPosition();
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            startUI();
        } else {
            stopUI();
        }
    }
}
