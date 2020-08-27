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

package ru.krivocraft.tortoise.core;

import android.Manifest;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.player.AndroidMediaService;
import ru.krivocraft.tortoise.core.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.thumbnail.Colors;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class BaseActivity extends AppCompatActivity {

    private SettingsStorageManager settingsManager;
    private TracksStorageManager tracksStorageManager;
    private Colors colors;
    private MediaBrowserCompat mediaBrowser;

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;
    private static final int PERMISSION_READ_EXTERNAL_STORAGE = 22893;

    MediaControllerCompat mediaController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsStorageManager(this);
        colors = new Colors(this);
        tracksStorageManager = new TracksStorageManager(this);
        setTheme();
    }

    public final void setTheme() {
        boolean useLightTheme = settingsManager.get(SettingsStorageManager.KEY_THEME, false);

        if (useLightTheme) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestStoragePermission();

        IntentFilter colorFilter = new IntentFilter(Colors.ACTION_RESULT_COLOR);
        registerReceiver(interfaceRecolorReceiver, colorFilter);

        sendBroadcast(new Intent(Colors.ACTION_REQUEST_COLOR));
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            onPermissionGranted();
        }
    }

    public abstract void init();

    public abstract void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState);

    public abstract void onMetadataChanged(MediaMetadataCompat newMetadata);

    public abstract void onMediaBrowserConnected();

    private void initMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(
                BaseActivity.this,
                new ComponentName(BaseActivity.this, AndroidMediaService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                            mediaController = new MediaControllerCompat(BaseActivity.this, token);
                            mediaController.registerCallback(callback);
                            onMediaBrowserConnected();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.e("TAG", "onConnectionFailed");
                        Toast.makeText(BaseActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionSuspended() {
                        Log.e("TAG", "onConnectionSuspended");
                    }
                },
                null);
    }

    private final MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            BaseActivity.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            boolean useLightTheme = settingsManager.get(SettingsStorageManager.KEY_THEME, false);
            if (useLightTheme && Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                int color = colors.getColor(
                        tracksStorageManager
                                .getTrack(tracksStorageManager
                                        .getReference(metadata
                                                .getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)))
                                .getColor());
                recolorInterface(color);
            }
            BaseActivity.this.onMetadataChanged(metadata);
        }
    };

    private void recolorInterface(int color) {
        getWindow().setNavigationBarColor(color);
        getWindow().setStatusBarColor(color);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE || requestCode == PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                onPermissionGranted();
            } else {
                Toast.makeText(this, "App requires external storage permission to work", Toast.LENGTH_LONG).show();
                finishAffinity();
            }
        }
    }

    private void onPermissionGranted() {
        init();
        initMediaBrowser();
        mediaBrowser.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaBrowser != null) {
            mediaBrowser.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaController.unregisterCallback(callback);
        unregisterReceiver(interfaceRecolorReceiver);
    }

    private final BroadcastReceiver interfaceRecolorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean useLightTheme = settingsManager.get(SettingsStorageManager.KEY_THEME, false);
            if (useLightTheme && Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                int color = intent.getIntExtra(Colors.EXTRA_COLOR, -1);
                if (color != -1) {
                    int formattedColor = colors.getColor(color);
                    recolorInterface(formattedColor);
                }
            }
        }
    };
}
