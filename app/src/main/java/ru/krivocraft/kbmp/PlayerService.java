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
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerService extends Service {

    private MediaPlayer player;
    private Binder mBinder = new LocalBinder();

    private Playlist currentPlaylist;
    private Composition currentComposition;

    private int currentCompositionProgress = 0;

    private boolean isPlaying = false;

    private List<OnCompositionChangedListener> listeners = new ArrayList<>();
    private final int NOTIFY_ID = 124;
    private NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        PlayerService getServerInstance() {
            return PlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentPlaylist = new Playlist();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void start() {

        if (player != null) {
            stop();
        }

        player = new MediaPlayer();
        try {
            player.setDataSource(currentComposition.getPath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.seekTo(currentCompositionProgress);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextComposition();
            }
        });
        player.start();
        isPlaying = true;

        showNotification();
    }

    private void showNotification() {
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

        notificationLayout.setTextViewText(R.id.notification_composition_author, currentComposition.getComposer());
        notificationLayout.setTextViewText(R.id.notification_composition_name, currentComposition.getName());

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContent(notificationLayout)
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFY_ID, notification);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_PLAY);
            filter.addAction(Constants.ACTION_PAUSE);
            filter.addAction(Constants.ACTION_NEXT);
            filter.addAction(Constants.ACTION_PREVIOUS);
            registerReceiver(receiver, filter);
        } else {
            Toast.makeText(this, "Impossible to create service notification", Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("broadcast received with action: " + intent.getAction());
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
            }
            resetNotification();
        }
    };

    private void resetNotification(){
        hideNotification();
        showNotification();
    }

    void nextComposition() {
        newComposition(currentPlaylist.indexOf(currentComposition) + 1);
    }

    void previousComposition() {
        newComposition(currentPlaylist.indexOf(currentComposition) - 1);
    }

    Composition getCurrentComposition() {
        return currentComposition;
    }

    Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    void newComposition(int compositionIndex) {
        if (compositionIndex >= 0 && compositionIndex < currentPlaylist.getSize()) {
            stop();
            currentCompositionProgress = 0;
            currentComposition = currentPlaylist.getComposition(compositionIndex);

            for (OnCompositionChangedListener listener : listeners) {
                listener.onCompositionChanged();
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
            hideNotification();
        }
    }

    private void hideNotification() {
        notificationManager.cancel(NOTIFY_ID);
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
            return 0;
        }
    }

    void setCurrentCompositionProgress(int currentCompositionProgress) {
        this.currentCompositionProgress = currentCompositionProgress;
    }

    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    void addListener(OnCompositionChangedListener listener) {
        listeners.add(listener);
    }

    void removeListener(OnCompositionChangedListener listener) {
        listeners.remove(listener);
    }
}
