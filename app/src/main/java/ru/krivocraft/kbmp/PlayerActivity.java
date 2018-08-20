package ru.krivocraft.kbmp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {

    private SeekBar compositionProgressBar;

    private boolean isPlaying = false;

    private int compositionProgressInt = 0;

    private Timer compositionProgressTimer;
    private TextView compositionProgressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI(getIntent());
    }

    private void initUI(Intent intent) {
        TextView compositionNameTextView = findViewById(R.id.composition_name);
        TextView compositionAuthorTextView = findViewById(R.id.composition_author);
        TextView compositionDurationTextView = findViewById(R.id.composition_duration);
        compositionProgressTextView = findViewById(R.id.composition_progress);

        compositionProgressBar = findViewById(R.id.composition_progress_bar);

        String compositionName = intent.getStringExtra(Constants.COMPOSITION_NAME);
        String compositionAuthor = intent.getStringExtra(Constants.COMPOSITION_AUTHOR);
        String compositionDuration = intent.getStringExtra(Constants.COMPOSITION_DURATION);

        compositionNameTextView.setText(compositionName);
        compositionAuthorTextView.setText(compositionAuthor);
        compositionDurationTextView.setText(getFormattedTime(Integer.parseInt(compositionDuration)));

        compositionProgressBar.setMax(Integer.parseInt(compositionDuration));
    }

    private String getFormattedTime(int time) {
        int seconds = time % 60;
        int minutes = (time - seconds) / 60;

        String formattedSeconds = String.valueOf(seconds);
        String formattedMinutes = String.valueOf(minutes);

        if (seconds < 10) {
            formattedSeconds = "0" + formattedSeconds;
        }

        if (minutes < 10) {
            formattedMinutes = "0" + formattedMinutes;
        }


        return formattedMinutes + ":" + formattedSeconds;
    }

    private void updateProgress() {
        compositionProgressInt++;
    }

    private void updateBar() {
        compositionProgressBar.setProgress(compositionProgressInt);
        compositionProgressTextView.setText(getFormattedTime(compositionProgressInt));
    }

    private void startPlaying() {
        compositionProgressTimer = new Timer();
        compositionProgressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateProgress();
                        updateBar();
                    }
                });
            }
        }, 0, 1000);
    }

    private void stopPlaying() {
        compositionProgressTimer = null;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_pause:
                if (!isPlaying) {
                    startPlaying();
                    isPlaying = true;
                } else {
                    stopPlaying();
                    isPlaying = false;
                }
                break;
            case R.id.previous:
                break;
            case R.id.next:
                break;
        }
    }
}
