package ru.krivocraft.kbmp;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.File;

public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, Track.OnTrackStateChangedListener {

    private PlayerService serviceInstance;
    private Context context;
    private ImageButton playPauseButton;
    private TextView compositionNameTextView;
    private TextView compositionAuthorTextView;
    private TextView compositionProgressTextView;
    private TextView compositionDurationTextView;
    private SeekBar compositionProgressBar;
    private ImageView trackImage;

    public PlayerFragment() {

    }

    void setServiceInstance(PlayerService serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    void setContext(Context context) {
        this.context = context;
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        serviceInstance.start(seekBar.getProgress() * 1000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
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
                serviceInstance.previousComposition();
            }
        });

        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceInstance.nextComposition();
            }
        });

        updateUI();

        return rootView;
    }

    void updateUI() {
        Track currentTrack = serviceInstance.getCurrentTrack();

        if (currentTrack != null) {

            int progress = Utils.getSeconds(serviceInstance.getPlayerProgress());

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
                startUIPlaying();
            } else {
                stopUIPlaying();
            }

            playPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (serviceInstance.isPlaying()) {
                        serviceInstance.stop();
                    } else {
                        serviceInstance.start();
                    }
                }
            });

            compositionProgressBar.setMax(Integer.parseInt(compositionDuration) / 1000);
        }

    }

    void updateBar() {
        int duration = Integer.parseInt(serviceInstance.getCurrentTrack().getDuration());

        int progressMillis = serviceInstance.getPlayerProgress();
        int estimatedMillis = duration - progressMillis;

        int progress = Utils.getSeconds(progressMillis);
        int estimated = Utils.getSeconds(estimatedMillis) - 1;

        if (progress > compositionProgressBar.getMax()) {
            serviceInstance.stop();
        } else {
            compositionProgressBar.setProgress(progress);
            compositionProgressTextView.setText(Utils.getFormattedTime(progress));
            compositionDurationTextView.setText(String.format("-%s", Utils.getFormattedTime(estimated)));
        }
    }


    void startUIPlaying() {
        playPauseButton.setImageResource(R.drawable.ic_pause);
    }

    void stopUIPlaying() {
        playPauseButton.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void onTrackStateChanged(Track.TrackState state) {
        switch (state) {
            case NEW_TRACK:
                updateUI();
                break;
        }

    }
}
