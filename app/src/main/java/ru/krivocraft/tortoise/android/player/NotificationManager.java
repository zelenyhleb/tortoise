/*
 * Copyright (c) 2020 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.android.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.android.MainActivity;
import ru.krivocraft.tortoise.android.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.core.utils.Art;
import ru.krivocraft.tortoise.android.thumbnail.Colors;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationManager {

    private static final int NOTIFY_ID = 124;
    private final Service context;
    private final NotificationCompat.Action playAction;
    private final NotificationCompat.Action pauseAction;
    private final NotificationCompat.Action nextAction;
    private final NotificationCompat.Action previousAction;
    private final NotificationCompat.Action stopAction;
    private final TracksStorageManager tracksStorageManager;
    private final Colors colors;
    private final android.app.NotificationManager notificationManager;


    public NotificationManager(Service context) {
        this.context = context;
        notificationManager = (android.app.NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        this.tracksStorageManager = new TracksStorageManager(context);
        this.colors = new Colors(context);

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

    public void showNotification(MediaSessionCompat mediaSession) {
        if (mediaSession.getController().getMetadata() != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class)
                            .putExtra(MainActivity.ACTION_SHOW_PLAYER, true)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_CANCEL_CURRENT);

            androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
            mediaStyle.setMediaSession(mediaSession.getSessionToken());
            mediaStyle.setShowCancelButton(true);
            mediaStyle.setShowActionsInCompactView(1, 2, 3);


            String CHANNEL_ID = "channel_01";

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .addAction(stopAction)
                    .addAction(previousAction)
                    .setContentTitle(mediaSession.getController().getMetadata().getDescription().getTitle())
                    .setContentText(mediaSession.getController().getMetadata().getDescription().getSubtitle())
                    .setContentIntent(contentIntent)
                    .setSound(null)
                    .setCategory(Notification.CATEGORY_PROGRESS)
                    .setStyle(mediaStyle)
                    .setColorized(true)
                    .setShowWhen(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            String path = mediaSession.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
            Bitmap image = new Art(path).bitmap();

            if (image != null) {
                notificationBuilder.setLargeIcon(image);
            } else {
                Drawable drawable = context.getDrawable(R.drawable.ic_default_track_image_notification);
                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                notificationBuilder.setLargeIcon(bitmap);
                int color = colors.getColor(tracksStorageManager.getTrack(tracksStorageManager.getReference(path)).getColor());
                notificationBuilder.setColor(color);
            }

            boolean playing = mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel =
                        new NotificationChannel(CHANNEL_ID,
                                playing ? "Playing" : "Paused",
                                android.app.NotificationManager.IMPORTANCE_DEFAULT);

                channel.setImportance(android.app.NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(playing ? "Playing" : "Paused");
                notificationBuilder.setChannelId(CHANNEL_ID);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            if (playing) {
                context.startForeground(NOTIFY_ID, notificationBuilder.addAction(pauseAction).setOngoing(true).setSmallIcon(R.drawable.ic_play).addAction(nextAction).build());
            } else {
                if (notificationManager != null) {
                    context.stopForeground(false);
                    notificationManager.notify(NOTIFY_ID, notificationBuilder.addAction(playAction).setSmallIcon(R.drawable.ic_pause).addAction(nextAction).build());
                }
            }
        }
    }

    public void dismissNotification() {
        context.stopForeground(true);
        notificationManager.cancelAll();
    }

}
