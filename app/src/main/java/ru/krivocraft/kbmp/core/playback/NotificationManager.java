/*
 * Copyright (c) 2019 Nikifor Fedorov
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
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.kbmp.core.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
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
import ru.krivocraft.kbmp.core.utils.Art;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationManager {

    private static final int NOTIFY_ID = 124;
    private Service context;
    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action previousAction;
    private NotificationCompat.Action stopAction;
    private TracksStorageManager tracksStorageManager;
    private ColorManager colorManager;
    private android.app.NotificationManager notificationManager;


    public NotificationManager(Service context) {
        this.context = context;
        notificationManager = (android.app.NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

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

    public void showNotification(MediaSessionCompat mediaSession) {
        if (mediaSession.getController().getMetadata() != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, PlayerActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

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
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_track_image_default));
                int color = colorManager.getColor(tracksStorageManager.getTrack(tracksStorageManager.getReference(path)).getColor());
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
