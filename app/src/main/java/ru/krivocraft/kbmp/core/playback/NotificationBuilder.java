package ru.krivocraft.kbmp.core.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.contexts.PlayerActivity;
import ru.krivocraft.kbmp.core.ColorManager;
import ru.krivocraft.kbmp.core.storage.TracksStorageManager;
import ru.krivocraft.kbmp.core.utils.BitmapUtils;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationBuilder {

    static final int NOTIFY_ID = 124;
    private Context context;
    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action previousAction;
    private NotificationCompat.Action stopAction;
    private TracksStorageManager tracksStorageManager;
    private ColorManager colorManager;


    public NotificationBuilder(Context context) {
        this.context = context;

        this.tracksStorageManager = new TracksStorageManager(context);
        this.colorManager = new ColorManager(context);

        playAction = new NotificationCompat.Action(R.drawable.ic_play, "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PLAY));

        pauseAction = new NotificationCompat.Action(R.drawable.ic_pause, "pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PAUSE));

        nextAction = new NotificationCompat.Action(R.drawable.ic_next, "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        previousAction = new NotificationCompat.Action(R.drawable.ic_previous, "previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        stopAction = new NotificationCompat.Action(R.drawable.ic_close, "stop",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_STOP));
    }

    Notification getNotification(MediaSessionCompat mediaSession) {
        if (mediaSession.getController().getMetadata() != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, PlayerActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

            androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
            mediaStyle.setMediaSession(mediaSession.getSessionToken());
            mediaStyle.setShowCancelButton(true);
            mediaStyle.setShowActionsInCompactView(1, 2, 3);


            NotificationManager service = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            String CHANNEL_ID = "channel_01";

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .addAction(stopAction)
                    .addAction(previousAction)
                    .setContentTitle(mediaSession.getController().getMetadata().getDescription().getTitle())
                    .setContentText(mediaSession.getController().getMetadata().getDescription().getSubtitle())
                    .setContentIntent(contentIntent)
                    .setSound(null)
                    .setStyle(mediaStyle)
                    .setColorized(true)
                    .setOngoing(true)
                    .setShowWhen(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            String path = mediaSession.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
            Bitmap image = BitmapUtils.loadArt(path);

            if (image != null) {
                notificationBuilder.setLargeIcon(image);
            } else {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_track_image_default));
                int color = colorManager.getColor(tracksStorageManager.getTrack(tracksStorageManager.getReference(path)).getColor());
                notificationBuilder.setColor(color);
            }

            boolean playing = mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
            if (playing) {
                notificationBuilder.addAction(pauseAction).setSmallIcon(R.drawable.ic_play);
            } else {
                notificationBuilder.addAction(playAction).setSmallIcon(R.drawable.ic_pause);
            }

            notificationBuilder.addAction(nextAction);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel =
                        new NotificationChannel(CHANNEL_ID,
                                "Tortoise",
                                NotificationManager.IMPORTANCE_DEFAULT);

                channel.setImportance(NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(playing ? "Playing" : "Paused");
                notificationBuilder.setChannelId(CHANNEL_ID);
                if (service != null) {
                    service.createNotificationChannel(channel);
                }
            }

            return notificationBuilder.build();
        }
        return null;
    }

}
