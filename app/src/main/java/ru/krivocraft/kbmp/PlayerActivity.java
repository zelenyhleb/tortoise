package ru.krivocraft.kbmp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity {

    public static final String COMPOSITION_NAME = "compositionname";
    public static final String COMPOSITION_AUTHOR = "compositionauthor";
    public static final String COMPOSITION_DURATION = "compositionduration";

    private TextView compositionName;
    private TextView compositionAuthor;
    private TextView compositionDuration;
    private TextView compositionProgress;

    private SeekBar compositionProgressBar;

    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compositionName = findViewById(R.id.composition_name);
        compositionAuthor = findViewById(R.id.composition_author);
        compositionDuration = findViewById(R.id.composition_duration);
        compositionProgress = findViewById(R.id.composition_progress);
        compositionProgressBar = findViewById(R.id.composition_progress_bar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent in = getIntent();

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_pause:
                if (!isPlaying) {

                } else {

                }
                break;
            case R.id.previous:
                break;
            case R.id.next:
                break;
        }
    }
}
