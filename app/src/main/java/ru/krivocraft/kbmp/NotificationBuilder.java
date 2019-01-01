package ru.krivocraft.kbmp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

class NotificationBuilder {
    private Context context;

    NotificationBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    void updateNotification(MediaSessionCompat mediaSession) {
        if (mediaSession.getController().getMetadata() != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, TortoiseActivity.class).setAction(Constants.ACTION_SHOW_PLAYER), PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Action playAction = new NotificationCompat.Action(R.drawable.ic_play, "play", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY));
            NotificationCompat.Action pauseAction = new NotificationCompat.Action(R.drawable.ic_pause, "pause", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE));
            NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_next, "next", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
            NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_previous, "previous", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

            android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle mediaStyle = new android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle();
            mediaStyle.setMediaSession(mediaSession.getSessionToken());
            mediaStyle.setShowCancelButton(true);
            mediaStyle.setShowActionsInCompactView(1);


            NotificationManager service = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            String CHANNEL_ID = "channel_01";

            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .addAction(previousAction)
                    .setContentTitle(mediaSession.getController().getMetadata().getDescription().getTitle())
                    .setContentText(mediaSession.getController().getMetadata().getDescription().getSubtitle())
                    .setSound(null)
                    .setStyle(mediaStyle)
                    .setContentIntent(contentIntent);

            if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                notification.addAction(pauseAction).setSmallIcon(R.drawable.ic_play).setOngoing(true);
            } else {
                notification.addAction(playAction).setSmallIcon(R.drawable.ic_pause).setOngoing(false);
            }

            notification.addAction(nextAction);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Tortoise", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setImportance(NotificationManager.IMPORTANCE_LOW);
                notification.setChannelId(CHANNEL_ID);
                if (service != null) {
                    service.createNotificationChannel(channel);
                }
            }

            if (service != null) {
                service.notify(Constants.NOTIFY_ID, notification.build());
            }
        }
    }

    void removeNotification() {
        NotificationManager service = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (service != null) {
            service.cancel(Constants.NOTIFY_ID);
        }
    }

}
