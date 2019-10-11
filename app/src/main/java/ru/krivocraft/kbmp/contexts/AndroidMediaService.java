package ru.krivocraft.kbmp.contexts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

import ru.krivocraft.kbmp.core.playback.MediaService;

public class AndroidMediaService extends MediaBrowserServiceCompat {

    public static boolean running = false;

    private MediaService mediaService;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    @Override
    public void onDestroy() {
        this.mediaService.destroy();
        running = false;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mediaService = new MediaService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mediaService.handleCommand(intent);
        running = true;
        return START_NOT_STICKY;
    }

}
