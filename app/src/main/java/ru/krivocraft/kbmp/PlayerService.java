package ru.krivocraft.kbmp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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


        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);


        RemoteViews notificationLayout = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
//        notificationLayout.setImageViewResource(R.id.notification_play_pause, R.drawable.ic_play);
//        notificationLayout.setImageViewResource(R.id.notification_next, R.drawable.ic_next);
//        notificationLayout.setImageViewResource(R.id.notification_previous, R.drawable.ic_previous);
        notificationLayout.setTextViewText(R.id.notification_composition_author, currentComposition.getComposer());
        notificationLayout.setTextViewText(R.id.notification_composition_name, currentComposition.getName());

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContent(notificationLayout)
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);

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
            notificationManager.cancel(NOTIFY_ID);
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
