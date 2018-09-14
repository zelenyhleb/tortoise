package ru.krivocraft.kbmp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

public class PlayerFragment extends Fragment {

    private String compositionAuthor;
    private String compositionName;
    private int compositionProgress;
    private int compositionDuration;
    private boolean compositionState;
    private String compositionPath;
    private Timer progressBarTimer;
    private View rootView;
    private Context context;

    public PlayerFragment() {
    }

    void setData(Track track, int compositionProgress, int compositionDuration, boolean compositionState) {
        this.compositionAuthor = track.getArtist();
        this.compositionName = track.getName();
        this.compositionProgress = compositionProgress;
        this.compositionDuration = compositionDuration;
        this.compositionState = compositionState;
        this.compositionPath = track.getPath();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_player, container, false);
        context = getContext();
        initStaticUI();
        initNonStaticUI();
        return rootView;
    }

    void initStaticUI() {
        if (context != null) {
            rootView.findViewById(R.id.text_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(context, PlayerActivity.class));
                }
            });

            final TextView viewAuthor = rootView.findViewById(R.id.fragment_composition_author);
            final TextView viewName = rootView.findViewById(R.id.fragment_composition_name);
            final ImageView viewImage = rootView.findViewById(R.id.fragment_track_image);

            viewAuthor.setText(compositionAuthor);
            viewName.setText(compositionName);
            viewName.setSelected(true);

            Track.GetBitmapTask task = new Track.GetBitmapTask();
            task.setListener(new Track.OnPictureProcessedListener() {
                @Override
                public void onPictureProcessed(final Bitmap bitmap) {
                    if (bitmap != null) {
                        viewImage.setImageBitmap(bitmap);
                    } else {
                        viewImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_track_image_default));
                    }
                    viewImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadein));
                }
            });
            task.execute(new File(compositionPath));

            ImageButton previousCompositionButton = rootView.findViewById(R.id.fragment_button_previous);
            ImageButton nextCompositionButton = rootView.findViewById(R.id.fragment_button_next);

            previousCompositionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        PendingIntent.getBroadcast(context, 0, new Intent().setAction(Constants.ACTION_PREVIOUS), PendingIntent.FLAG_CANCEL_CURRENT).send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            });
            nextCompositionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        PendingIntent.getBroadcast(context, 0, new Intent().setAction(Constants.ACTION_NEXT), PendingIntent.FLAG_CANCEL_CURRENT).send();
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
            bar.setMax(compositionDuration);
            bar.setProgress(compositionProgress);

            ImageButton playPauseCompositionButton = rootView.findViewById(R.id.fragment_button_playpause);
            if (compositionState) {
                playPauseCompositionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                playPauseCompositionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            PendingIntent.getBroadcast(context, 0, new Intent().setAction(Constants.ACTION_PAUSE), PendingIntent.FLAG_CANCEL_CURRENT).send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                });
                progressBarTimer = new Timer();
                progressBarTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        bar.setProgress(bar.getProgress() + 1);
                    }
                }, 0, 1000);
            } else {
                playPauseCompositionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                playPauseCompositionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            PendingIntent.getBroadcast(context, 0, new Intent().setAction(Constants.ACTION_PLAY), PendingIntent.FLAG_CANCEL_CURRENT).send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                });
                progressBarTimer.cancel();
            }
        }
    }
}
