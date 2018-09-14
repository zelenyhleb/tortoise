package ru.krivocraft.kbmp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerService extends Service implements Track.OnTrackStateChangedListener {

    private MediaPlayer player;
    private Binder mBinder = new LocalBinder();

    private Playlist currentPlaylist;
    private Track currentTrack;

    private int currentCompositionProgress = 0;

    private boolean isPlaying = false;

    private List<Track.OnTrackStateChangedListener> listeners = new ArrayList<>();
    private final int NOTIFY_ID = 124;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onNewTrackState() {
        updateNotification();
    }

    class LocalBinder extends Binder {
        PlayerService getServerInstance() {
            return PlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentPlaylist = new Playlist();
        addListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_PLAY);
        filter.addAction(Constants.ACTION_PAUSE);
        filter.addAction(Constants.ACTION_NEXT);
        filter.addAction(Constants.ACTION_PREVIOUS);
        filter.addAction(Constants.ACTION_CLOSE);
        registerReceiver(receiver, filter);

        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        sendBroadcast(new Intent().setAction(Constants.ACTION_PREVIOUS));
        unregisterReceiver(receiver);
    }

    public void start() {
        start(currentCompositionProgress);
    }

    public void start(int progress) {

        if (player != null) {
            stop();
        }

        player = new MediaPlayer();
        try {
            player.setDataSource(currentTrack.getPath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.seekTo(progress);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextComposition();
            }
        });
        player.start();
        isPlaying = true;

        for (Track.OnTrackStateChangedListener listener : listeners) {
            listener.onNewTrackState();
        }
    }

    private void updateNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PlayerActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent playIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_PLAY), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_PAUSE), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent nextCompositionIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_NEXT), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent previousCompositionIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_PREVIOUS), PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews notificationLayout = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);

        if (isPlaying) {
            notificationLayout.setImageViewResource(R.id.notification_play_pause, R.drawable.ic_pause);
            notificationLayout.setOnClickPendingIntent(R.id.notification_play_pause, pauseIntent);
        } else {
            notificationLayout.setImageViewResource(R.id.notification_play_pause, R.drawable.ic_play);
            notificationLayout.setOnClickPendingIntent(R.id.notification_play_pause, playIntent);
        }

        notificationLayout.setImageViewResource(R.id.notification_next, R.drawable.ic_next);
        notificationLayout.setOnClickPendingIntent(R.id.notification_next, nextCompositionIntent);

        notificationLayout.setImageViewResource(R.id.notification_previous, R.drawable.ic_previous);
        notificationLayout.setOnClickPendingIntent(R.id.notification_previous, previousCompositionIntent);

        notificationLayout.setTextViewText(R.id.notification_composition_author, currentTrack.getArtist());
        notificationLayout.setTextViewText(R.id.notification_composition_name, currentTrack.getName());

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContent(notificationLayout)
                .setContentIntent(contentIntent)
                .build();

        startForeground(NOTIFY_ID, notification);
    }

    private void dismissNotification() {
        stopForeground(true);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case Constants.ACTION_PLAY:
                    start();
                    break;
                case Constants.ACTION_PAUSE:
                    stop();
                    break;
                case Constants.ACTION_NEXT:
                    nextComposition();
                    break;
                case Constants.ACTION_PREVIOUS:
                    previousComposition();
                    break;
                case Constants.ACTION_CLOSE:
                    dismissNotification();
                    break;
            }
        }
    };

    void nextComposition() {
        System.out.println(currentPlaylist.indexOf(currentTrack));
        newComposition(currentPlaylist.indexOf(currentTrack) + 1);
    }

    void previousComposition() {
        newComposition(currentPlaylist.indexOf(currentTrack) - 1);
    }

    Track getCurrentTrack() {
        return currentTrack;
    }

    Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    void newComposition(int compositionIndex) {
        if (compositionIndex >= 0 && compositionIndex < currentPlaylist.getSize()) {
            stop();
            currentCompositionProgress = 0;
            currentTrack = currentPlaylist.getComposition(compositionIndex);

            for (Track.OnTrackStateChangedListener listener : listeners) {
                listener.onNewTrackState();
            }

            start();
        }
    }

    void stop() {
        if (player != null) {
            currentCompositionProgress = player.getCurrentPosition();
            player.stop();

            release();

            isPlaying = false;

            for (Track.OnTrackStateChangedListener listener : listeners) {
                listener.onNewTrackState();
            }
        }
    }

    private void release() {
        player.release();
        player = null;
    }

    boolean isPlaying() {
        return isPlaying;
    }

    int getProgress() {
        if (player != null) {
            return player.getCurrentPosition();
        } else {
            return currentCompositionProgress;
        }
    }

    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    void addListener(Track.OnTrackStateChangedListener listener) {
        listeners.add(listener);
    }

    void removeListener(Track.OnTrackStateChangedListener listener) {
        listeners.remove(listener);
    }
}
