package ru.krivocraft.kbmp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class SmallPlayerFragment extends Fragment implements StateCallback {

    private Timer progressBarTimer;
    private View rootView;
    private Context context;

    private String trackArtist;
    private String trackTitle;
    private int trackDuration;
    private int trackProgress;
    private boolean trackIsPlaying;

    public SmallPlayerFragment() {
    }

    void setContext(Context context) {
        this.context = context;
    }

    void setInitialData(String trackArtist, String trackTitle, int trackDuration, int trackProgress, boolean trackIsPlaying){
        this.trackArtist = trackArtist;
        this.trackTitle = trackTitle;
        this.trackDuration = trackDuration;
        this.trackProgress = trackProgress;
        this.trackIsPlaying = trackIsPlaying;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_player_small, container, false);
        initStaticUI();
        initNonStaticUI();
        return rootView;
    }

    void initStaticUI() {
        if (context != null) {
            rootView.findViewById(R.id.text_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    if (context != null)
                        context.sendBroadcast(new Intent(Constants.ACTION_SHOW_PLAYER));
                }
            });

            final TextView viewAuthor = rootView.findViewById(R.id.fragment_composition_author);
            final TextView viewName = rootView.findViewById(R.id.fragment_composition_name);
            final ImageView viewImage = rootView.findViewById(R.id.fragment_track_image);

            viewAuthor.setText(trackArtist);
            viewName.setText(trackTitle);
            viewName.setSelected(true);

//            GetBitmapTask task = new GetBitmapTask();
//            task.setListener(new OnPictureProcessedListener() {
//                @Override
//                public void onPictureProcessed(final Bitmap bitmap) {
//                    if (bitmap != null) {
//                        viewImage.setImageBitmap(bitmap);
//                    } else {
//                        viewImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_track_image_default));
//                    }
//                    viewImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadein));
//                }
//            });
//            task.execute(new File(currentTrack.getPath()));

            ImageButton previousCompositionButton = rootView.findViewById(R.id.fragment_button_previous);
            ImageButton nextCompositionButton = rootView.findViewById(R.id.fragment_button_next);

            previousCompositionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            });
            nextCompositionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT).send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    void initNonStaticUI() {
        if (context != null) {
            final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
            bar.setMax(Utils.getSeconds(trackDuration));
            bar.setProgress(Utils.getSeconds(trackProgress));

            ImageButton playPauseCompositionButton = rootView.findViewById(R.id.fragment_button_playpause);
            if (trackIsPlaying) {
                playPauseCompositionButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause));
                playPauseCompositionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE).send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                });
                cancelCurrentTimer();
                startNewTimer(bar);
            } else {
                playPauseCompositionButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play));
                playPauseCompositionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY).send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                });
                cancelCurrentTimer();
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

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        trackArtist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        trackTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        trackDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        initStaticUI();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        trackIsPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        trackProgress = (int) playbackState.getPosition();
        initNonStaticUI();
    }
}
