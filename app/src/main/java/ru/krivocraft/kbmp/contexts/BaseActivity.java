package ru.krivocraft.kbmp.contexts;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.RemoteException;
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

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.ColorManager;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;
import ru.krivocraft.kbmp.core.storage.TracksStorageManager;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class BaseActivity extends AppCompatActivity {

    private SettingsStorageManager settingsManager;
    private TracksStorageManager tracksStorageManager;
    private ColorManager colorManager;
    private MediaBrowserCompat mediaBrowser;

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;

    MediaControllerCompat mediaController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsStorageManager(this);
        colorManager = new ColorManager(this);
        tracksStorageManager = new TracksStorageManager(this);
        setTheme();
        requestStoragePermission();

    }

    final void setTheme() {
        boolean useLightTheme = settingsManager.getOption(SettingsStorageManager.KEY_THEME, false);

        if (useLightTheme) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter colorFilter = new IntentFilter(ColorManager.ACTION_RESULT_COLOR);
        registerReceiver(interfaceRecolorReceiver, colorFilter);

        sendBroadcast(new Intent(ColorManager.ACTION_REQUEST_COLOR));
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            onPermissionGranted();
        }
    }

    abstract void init();

    abstract void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState);

    abstract void onMetadataChanged(MediaMetadataCompat newMetadata);

    abstract void onMediaBrowserConnected();

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
                        } catch (RemoteException e) {
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
            boolean useLightTheme = settingsManager.getOption(SettingsStorageManager.KEY_THEME, false);
            if (useLightTheme) {
                int color = colorManager.getColor(
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
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
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
    protected void onDestroy() {
        super.onDestroy();
        if (mediaBrowser != null) {
            mediaBrowser.disconnect();
        }
        unregisterReceiver(interfaceRecolorReceiver);
    }

    private final BroadcastReceiver interfaceRecolorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean useLightTheme = settingsManager.getOption(SettingsStorageManager.KEY_THEME, false);
            if (useLightTheme) {
                int color = intent.getIntExtra(ColorManager.EXTRA_COLOR, -1);
                if (color != -1) {
                    int formattedColor = colorManager.getColor(color);
                    recolorInterface(formattedColor);
                }
            }
        }
    };
}
