package ru.krivocraft.kbmp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SmallPlayerFragment extends Fragment {

    private Timer progressBarTimer;
    private View rootView;
    private MediaControllerCompat.TransportControls transportControls;

    private String trackArtist;
    private String trackTitle;
    private int trackDuration;
    private int trackProgress;
    private Bitmap trackArt;
    private boolean trackIsPlaying;
    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            trackIsPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
            trackProgress = (int) state.getPosition();
            invalidate();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            trackArtist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            trackTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            trackArt = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
            trackDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            invalidate();
        }
    };

    public SmallPlayerFragment() {
    }

    void init(Activity context, MediaMetadataCompat mediaMetadata, PlaybackStateCompat playbackState, int position) {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(context);
        this.transportControls = mediaController.getTransportControls();

        mediaController.registerCallback(callback);

        this.trackArtist = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        this.trackTitle = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        this.trackArt = mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
        this.trackDuration = (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

        this.trackProgress = position;
        this.trackIsPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_player_small, container, false);
        invalidate();
        return rootView;
    }

    void invalidate() {
        final Context context = getContext();
        rootView.findViewById(R.id.text_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        if (context != null) {
            if (trackArt != null) {
                viewImage.setImageBitmap(trackArt);
            } else {
                viewImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_track_image_default));
            }
            viewImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeinshort));
        }

        ImageButton previousCompositionButton = rootView.findViewById(R.id.fragment_button_previous);
        ImageButton nextCompositionButton = rootView.findViewById(R.id.fragment_button_next);

        previousCompositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transportControls.skipToPrevious();
            }
        });
        nextCompositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transportControls.skipToNext();
            }
        });

        final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
        bar.setMax(Utils.getSeconds(trackDuration));
        bar.setProgress(Utils.getSeconds(trackProgress));

        ImageButton playPauseCompositionButton = rootView.findViewById(R.id.fragment_button_playpause);
        if (trackIsPlaying) {
            playPauseCompositionButton.setImageResource(R.drawable.ic_pause);
            playPauseCompositionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transportControls.pause();
                }
            });
            cancelCurrentTimer();
            startNewTimer(bar);
        } else {
            playPauseCompositionButton.setImageResource(R.drawable.ic_play);
            playPauseCompositionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transportControls.play();
                }
            });
            cancelCurrentTimer();
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
    public void onDestroy() {
        super.onDestroy();
    }
}
