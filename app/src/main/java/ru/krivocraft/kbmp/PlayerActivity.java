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

import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, OnCompositionStateChangedListener {

    private SeekBar compositionProgressBar;

    private boolean isPlaying = false;
    private boolean mBounded = false;

    private Timer compositionProgressTimer;
    private TextView compositionProgressTextView;
    private ImageButton playPauseButton;

    private PlayerService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;

            PlayerService.LocalBinder localBinder = (PlayerService.LocalBinder) service;
            mService = localBinder.getServerInstance();

            mService.addListener(PlayerActivity.this);

            isPlaying = mService.isPlaying();

            initUI();
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
        bindService(new Intent(this, PlayerService.class), mConnection, BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBounded) {
            mService.removeListener(PlayerActivity.this);
            unbindService(mConnection);
            mBounded = false;
        }
    }

    private void initUI() {

        Composition currentComposition = mService.getCurrentComposition();

        playPauseButton = findViewById(R.id.play_pause);

        TextView compositionNameTextView = findViewById(R.id.composition_name);
        TextView compositionAuthorTextView = findViewById(R.id.composition_author);
        TextView compositionDurationTextView = findViewById(R.id.composition_duration);

        compositionProgressTextView = findViewById(R.id.composition_progress);

        double progressMillis = mService.getProgress() / 1000.0;
        int progress = (int) Math.ceil(progressMillis);

        compositionProgressTextView.setText(Utils.getFormattedTime(progress));

        compositionProgressBar = findViewById(R.id.composition_progress_bar);
        compositionProgressBar.setProgress(progress);
        compositionProgressBar.setOnSeekBarChangeListener(this);

        String compositionName = currentComposition.getName();
        String compositionComposer = currentComposition.getComposer();
        String compositionDuration = currentComposition.getDuration();

        compositionNameTextView.setText(compositionName);
        compositionAuthorTextView.setText(compositionComposer);
        compositionDurationTextView.setText(Utils.getFormattedTime(Integer.parseInt(compositionDuration) / 1000));

        compositionProgressBar.setMax(Integer.parseInt(compositionDuration) / 1000);

        if (mService.isPlaying()) {
            startUIPlaying();
        }
    }

    private void updateBar() {
        double progressMillis = mService.getProgress() / 1000.0;
        int progress = (int) Math.ceil(progressMillis);
        if (progress > compositionProgressBar.getMax()) {
            stopPlaying();
        } else {
            compositionProgressBar.setProgress(progress);
            compositionProgressTextView.setText(Utils.getFormattedTime(progress));
        }
    }


    private void startPlaying() {
        if (mBounded) {
            mService.start();
        }
    }

    private void stopPlaying() {
        if (mBounded) {
            mService.stop();
        }
    }

    private void startUIPlaying() {
        if (compositionProgressTimer == null) {
            compositionProgressTimer = new Timer();
            compositionProgressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateBar();
                        }
                    });
                }
            }, Constants.ZERO, Constants.ONE_SECOND);

            setPlayPauseButton();
        }
    }

    private void stopUIPlaying() {
        if (compositionProgressTimer != null) {
            compositionProgressTimer.cancel();
            compositionProgressTimer = null;

            setPlayPauseButton();
        }
    }

    private void setPlayPauseButton(){
        if (isPlaying){
            playPauseButton.setImageResource(R.drawable.ic_pause);
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play);
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
                previousComposition();
                break;
            case R.id.next:
                nextComposition();
                break;
        }
    }

    private void previousComposition() {
        if (mBounded) {
            mService.previousComposition();
        }
    }

    private void nextComposition() {
        if (mBounded) {
            mService.nextComposition();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        stopPlaying();
        mService.setCurrentCompositionProgress(seekBar.getProgress() * 1000);
        startPlaying();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onNewComposition() {
        stopUIPlaying();
        initUI();
        startUIPlaying();
    }

    @Override
    public void onPlayComposition() {
        isPlaying = true;
        startUIPlaying();
    }

    @Override
    public void onPauseComposition() {
        isPlaying = false;
        stopUIPlaying();
    }
}
