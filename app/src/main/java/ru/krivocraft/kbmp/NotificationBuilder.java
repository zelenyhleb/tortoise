package ru.krivocraft.kbmp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

class NotificationBuilder {

    private MediaPlaybackService context;
    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action previousAction;


    NotificationBuilder(MediaPlaybackService context) {
        this.context = context;

        playAction = new NotificationCompat.Action(R.drawable.ic_play, "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PLAY));

        pauseAction = new NotificationCompat.Action(R.drawable.ic_pause, "pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PAUSE));

        nextAction = new NotificationCompat.Action(R.drawable.ic_next, "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        previousAction = new NotificationCompat.Action(R.drawable.ic_previous, "previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
    }

    void updateNotification(MediaSessionCompat mediaSession) {
        if (mediaSession.getController().getMetadata() != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class).setAction(Constants.ACTION_SHOW_PLAYER), PendingIntent.FLAG_CANCEL_CURRENT);

            android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle mediaStyle = new android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle();
            mediaStyle.setMediaSession(mediaSession.getSessionToken());
            mediaStyle.setShowCancelButton(true);
            mediaStyle.setShowActionsInCompactView(1);


            NotificationManager service = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            String CHANNEL_ID = "channel_01";

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .addAction(previousAction)
                    .setContentTitle(mediaSession.getController().getMetadata().getDescription().getTitle())
                    .setContentText(mediaSession.getController().getMetadata().getDescription().getSubtitle())
                    .setSound(null)
                    .setStyle(mediaStyle)
                    .setColorized(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            Bitmap image = Utils.loadArt(mediaSession.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));

            if (image != null) {
                notificationBuilder.setLargeIcon(image);
            } else {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_track_image_default));
            }

            if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                notificationBuilder.addAction(pauseAction).setSmallIcon(R.drawable.ic_play).setOngoing(true);
            } else {
                notificationBuilder.addAction(playAction).setSmallIcon(R.drawable.ic_pause).setOngoing(false);
            }

            notificationBuilder.addAction(nextAction);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Tortoise", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setImportance(NotificationManager.IMPORTANCE_LOW);
                notificationBuilder.setChannelId(CHANNEL_ID);
                if (service != null) {
                    service.createNotificationChannel(channel);
                }
            }

            Notification notification = notificationBuilder.build();
            showNotification(notification);
        }
    }

    void showNotification(Notification notification) {
        context.startForeground(Constants.NOTIFY_ID, notification);
    }

    void removeNotification() {
        if (context != null) {
            context.stopForeground(true);
        }
    }

}
