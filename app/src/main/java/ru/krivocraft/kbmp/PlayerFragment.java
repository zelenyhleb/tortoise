package ru.krivocraft.kbmp;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class PlayerFragment extends Fragment {

    private String compositionAuthor;
    private String compositionName;
    private int compositionProgress;
    private int compositionDuration;
    private boolean compositionState;
    private Timer timer = new Timer();

    public PlayerFragment() {
    }

    void setData(String compositionAuthor, String compositionName, int compositionProgress, int compositionDuration, boolean compositionState) {
        this.compositionAuthor = compositionAuthor;
        this.compositionName = compositionName;
        this.compositionProgress = compositionProgress;
        this.compositionDuration = compositionDuration;
        this.compositionState = compositionState;
    }

    void destroy() {
        timer.cancel();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        rootView.findViewById(R.id.text_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PlayerActivity.class));
            }
        });

        TextView viewAuthor = rootView.findViewById(R.id.fragment_composition_author);
        TextView viewName = rootView.findViewById(R.id.fragment_composition_name);

        viewAuthor.setText(compositionAuthor);
        viewName.setText(compositionName);
        viewName.setSelected(true);

        final ProgressBar bar = rootView.findViewById(R.id.fragment_progressbar);
        bar.setMax(compositionDuration);
        bar.setProgress(compositionProgress);

        ImageButton playPauseCompositionButton = rootView.findViewById(R.id.fragment_button_playpause);
        ImageButton previousCompositionButton = rootView.findViewById(R.id.fragment_button_previous);
        ImageButton nextCompositionButton = rootView.findViewById(R.id.fragment_button_next);

        if (compositionState) {
            playPauseCompositionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
            playPauseCompositionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        PendingIntent.getBroadcast(getContext(), 0, new Intent().setAction(Constants.ACTION_PAUSE), PendingIntent.FLAG_CANCEL_CURRENT).send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            });

            timer.schedule(new TimerTask() {
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
                        PendingIntent.getBroadcast(getContext(), 0, new Intent().setAction(Constants.ACTION_PLAY), PendingIntent.FLAG_CANCEL_CURRENT).send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        previousCompositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PendingIntent.getBroadcast(getContext(), 0, new Intent().setAction(Constants.ACTION_PREVIOUS), PendingIntent.FLAG_CANCEL_CURRENT).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });
        nextCompositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PendingIntent.getBroadcast(getContext(), 0, new Intent().setAction(Constants.ACTION_NEXT), PendingIntent.FLAG_CANCEL_CURRENT).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }
}
