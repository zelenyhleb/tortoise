package ru.krivocraft.kbmp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import ru.krivocraft.kbmp.constants.Constants;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends BaseActivity {


    private SmallPlayerFragment smallPlayerFragment;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaControllerCompat;

    private boolean useAlternativeTheme;

    private int viewState = 0;
    private static final int STATE_EXPLORER = 1;
    private static final int STATE_TRACK_LIST = 2;

    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;

    private BroadcastReceiver showPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.Actions.ACTION_SHOW_PLAYER.equals(intent.getAction())) {
                showSmallPlayerFragment();
            } else if (Constants.Actions.ACTION_HIDE_PLAYER.equals(intent.getAction())) {
                hideSmallPlayerFragment();
            }
        }
    };
    private TrackListFragment trackListFragment;
    private ExplorerFragment explorerFragment;

    @NonNull
    private TrackListFragment getTrackListFragment(TrackList trackList) {
        trackListFragment = TrackListFragment.newInstance(trackList, true, this);
        return trackListFragment;
    }

    private ExplorerFragment getExplorerFragment() {
        explorerFragment = ExplorerFragment.newInstance(this::showTrackListFragment);
        return explorerFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeOldCache();

        requestStoragePermission();
    }

    private void removeOldCache() {
        SharedPreferences preferences = getSharedPreferences(Constants.STORAGE_TRACK_LISTS, MODE_PRIVATE);
        String identifier = "all_tracks";
        if (preferences.getString(identifier, null) != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(identifier);
            editor.apply();
        }
        SharedPreferences settings = getSharedPreferences(Constants.STORAGE_SETTINGS, MODE_PRIVATE);
        if (Utils.getOption(settings, Constants.KEY_OLD_TRACK_LISTS_EXIST, true)) {
            Utils.clearCache(preferences);
            Utils.putOption(settings, Constants.KEY_OLD_TRACK_LISTS_EXIST, false);
        }
    }

    private void init() {
        useAlternativeTheme = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("useAlternativeTheme", false);

        setContentView(R.layout.activity_tortoise);

        RelativeLayout layout = findViewById(R.id.main_layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        if (!MediaPlaybackService.running) {
            startService(new Intent(this, MediaPlaybackService.class));
        }

        showExplorerFragment();

        IntentFilter showPlayerFilter = new IntentFilter();
        showPlayerFilter.addAction(Constants.Actions.ACTION_SHOW_PLAYER);
        showPlayerFilter.addAction(Constants.Actions.ACTION_HIDE_PLAYER);
        registerReceiver(showPlayerReceiver, showPlayerFilter);

        mediaBrowser = new MediaBrowserCompat(
                MainActivity.this,
                new ComponentName(MainActivity.this, MediaPlaybackService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                            MediaControllerCompat controller = new MediaControllerCompat(MainActivity.this, token);
                            MediaControllerCompat.setMediaController(MainActivity.this, controller);
                            MainActivity.this.mediaControllerCompat = controller;
                            showSmallPlayerFragment();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.e("TAG", "onConnectionFailed");
                        Toast.makeText(MainActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionSuspended() {
                        Log.e("TAG", "onConnectionSuspended");
                    }
                },
                null);
        mediaBrowser.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExplorerFragment() {
        removeTrackListFragment();
        showFragment(R.anim.fadein, getExplorerFragment(), R.id.fragment_container);
        viewState = STATE_EXPLORER;
    }

    private void showTrackListFragment(TrackList trackList) {
        removeExplorerFragment();
        showFragment(R.anim.fadein, getTrackListFragment(trackList), R.id.fragment_container);
        viewState = STATE_TRACK_LIST;
    }

    @Override
    public void onBackPressed() {
        if (viewState == STATE_TRACK_LIST) {
            showExplorerFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        if (useAlternativeTheme) {
            theme.applyStyle(R.style.LightTheme, true);
        }
        return theme;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaBrowser != null) {
            mediaBrowser.disconnect();

            unregisterReceiver(showPlayerReceiver);
            unregisterReceiver(positionReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                Toast.makeText(this, "App requires external storage permission to work", Toast.LENGTH_LONG).show();
                finishAffinity();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (explorerFragment != null) {
            explorerFragment.invalidate();
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            init();
        }
    }

    private void removeExplorerFragment() {
        hideFragment(explorerFragment);
        explorerFragment = null;
    }

    private void removeTrackListFragment() {
        hideFragment(trackListFragment);
        trackListFragment = null;
    }

    private void showSmallPlayerFragment() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Actions.ACTION_RESULT_DATA);
        registerReceiver(positionReceiver, filter);

        Intent intent = new Intent(Constants.Actions.ACTION_REQUEST_DATA);
        sendBroadcast(intent);
    }

    BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaControllerCompat != null) {
                MediaMetadataCompat metadata = intent.getParcelableExtra(Constants.Extras.EXTRA_METADATA);
                PlaybackStateCompat playbackState = intent.getParcelableExtra(Constants.Extras.EXTRA_PLAYBACK_STATE);
                int position = intent.getIntExtra(Constants.Extras.EXTRA_POSITION, 0);

                if (metadata != null && playbackState != null) {
                    if (smallPlayerFragment != null) {
                        smallPlayerFragment.init(MainActivity.this, metadata, playbackState, position);
                        smallPlayerFragment.invalidate();
                    } else {
                        SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
                        smallPlayerFragment.init(MainActivity.this, metadata, playbackState, position);
                        MainActivity.this.smallPlayerFragment = smallPlayerFragment;
                        showFragment(R.anim.fadeinshort, MainActivity.this.smallPlayerFragment, R.id.player_container);
                    }
                }
            }
        }
    };

    private void hideSmallPlayerFragment() {
        hideFragment(smallPlayerFragment);
        smallPlayerFragment = null;
    }


    private void showFragment(int animationIn, Fragment fragment, int container) {
        if (fragment != null && !fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animationIn, R.anim.fadeoutshort)
                    .add(container, fragment)
                    .commitNowAllowingStateLoss();
        }
    }

    private void hideFragment(Fragment fragment) {
        if (fragment != null && fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitNowAllowingStateLoss();
        }
    }

}
