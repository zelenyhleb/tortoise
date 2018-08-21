package ru.krivocraft.kbmp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private SeekBar compositionProgressBar;
    private String compositionPath;

    private boolean isPlaying = false;
    private boolean mBounded = false;

    private int compositionProgressInt = 0;

    private Timer compositionProgressTimer;
    private TextView compositionProgressTextView;
    private ImageButton playPauseButton;

    private PlayerService mService;

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            PlayerService.LocalBinder localBinder = (PlayerService.LocalBinder) service;
            mService = localBinder.getServerInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI(getIntent());

        bindService(new Intent(this, PlayerService.class), mConnection, BIND_AUTO_CREATE | BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    private void initUI(Intent intent) {

        playPauseButton = findViewById(R.id.play_pause);

        TextView compositionNameTextView = findViewById(R.id.composition_name);
        TextView compositionAuthorTextView = findViewById(R.id.composition_author);
        TextView compositionDurationTextView = findViewById(R.id.composition_duration);
        compositionProgressTextView = findViewById(R.id.composition_progress);

        compositionProgressBar = findViewById(R.id.composition_progress_bar);
        compositionProgressBar.setOnSeekBarChangeListener(this);

        String compositionName = intent.getStringExtra(Constants.COMPOSITION_NAME);
        String compositionAuthor = intent.getStringExtra(Constants.COMPOSITION_AUTHOR);
        String compositionDuration = intent.getStringExtra(Constants.COMPOSITION_DURATION);

        compositionNameTextView.setText(compositionName);
        compositionAuthorTextView.setText(compositionAuthor);
        compositionDurationTextView.setText(Utils.getFormattedTime(Integer.parseInt(compositionDuration) / 1000));

        compositionProgressBar.setMax(Integer.parseInt(compositionDuration) / 1000);

        compositionPath = intent.getStringExtra(Constants.COMPOSITION_LOCATION);
    }

    private void updateProgress() {
        compositionProgressInt = compositionProgressInt + 1000;
        System.out.println(compositionProgressInt);
    }

    private void updateBar() {
        if (compositionProgressInt / 1000 > compositionProgressBar.getMax()) {
            stopPlaying();
        } else {
            compositionProgressBar.setProgress(compositionProgressInt / 1000);
            compositionProgressTextView.setText(Utils.getFormattedTime(compositionProgressInt / 1000));
        }
    }


    private void startPlaying() {
        if (mBounded) {
            try {
                mService.startPlaying(compositionPath, compositionProgressInt);
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

                playPauseButton.setImageResource(R.drawable.ic_pause);
                isPlaying = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopPlaying() {
        if (mBounded) {
            if (compositionProgressTimer != null) {
                compositionProgressInt = mService.stopPlaying();
                compositionProgressTimer.cancel();
                compositionProgressTimer = null;
                playPauseButton.setImageResource(R.drawable.ic_play);
                isPlaying = false;
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_pause:
                if (!isPlaying) {
                    startPlaying();
                } else {
                    stopPlaying();
                }
                break;
            case R.id.previous:
                break;
            case R.id.next:
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        stopPlaying();
        compositionProgressInt = seekBar.getProgress() * 1000;
        startPlaying();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
}
