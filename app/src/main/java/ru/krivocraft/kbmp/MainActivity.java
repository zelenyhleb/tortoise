package ru.krivocraft.kbmp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {


    private SmallPlayerFragment smallPlayerFragment;
    private LargePlayerFragment largePlayerFragment;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaControllerCompat;
    private List<String> trackList;
    private boolean useAlternativeTheme;

    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;

    private BroadcastReceiver trackListUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_UPDATE_STORAGE.equals(intent.getAction())) {
                MainActivity.this.trackList = intent.getStringArrayListExtra(Constants.EXTRA_TRACK_LIST);
            }
        }
    };

    private BroadcastReceiver showPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_SHOW_PLAYER.equals(intent.getAction())) {
                showSmallPlayerFragment();
            }
        }
    };
    private TracksFragment trackListFragment;

    @NonNull
    private TracksFragment getTrackListFragment() {
        TracksFragment tracksFragment = new TracksFragment();
        tracksFragment.init(true, this, null);
        return tracksFragment;
    }

    private SettingsPage getSettingsPage() {
        SettingsPage settingsPage = new SettingsPage();
        settingsPage.setContext(this);
        return settingsPage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        useAlternativeTheme = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("useAlternativeTheme", false);

        setContentView(R.layout.activity_tortoise);

        RelativeLayout layout = findViewById(R.id.main_layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ViewPager pager = findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter());
        pager.setCurrentItem(Constants.INDEX_FRAGMENT_PLAYLIST);

        IntentFilter trackListUpdateFilter = new IntentFilter();
        trackListUpdateFilter.addAction(Constants.ACTION_UPDATE_STORAGE);
        registerReceiver(trackListUpdateReceiver, trackListUpdateFilter);

        IntentFilter showPlayerFilter = new IntentFilter();
        showPlayerFilter.addAction(Constants.ACTION_SHOW_PLAYER);
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
    public void onBackPressed() {
        if (largePlayerFragment != null) {
            hideLargePlayerFragment();
            showSmallPlayerFragment();
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
        mediaBrowser.disconnect();
        unregisterReceiver(trackListUpdateReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "App requires external storage permission to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void showSmallPlayerFragment() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_RESULT_DATA);
        registerReceiver(positionReceiver, filter);

        Intent intent = new Intent(Constants.ACTION_REQUEST_DATA);
        sendBroadcast(intent);
    }

    BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaControllerCompat != null) {

                MediaMetadataCompat metadata = intent.getParcelableExtra(Constants.EXTRA_METADATA);
                PlaybackStateCompat playbackState = intent.getParcelableExtra(Constants.EXTRA_PLAYBACK_STATE);
                int position = intent.getIntExtra(Constants.EXTRA_POSITION, 0);

                if (metadata != null && playbackState != null) {
                    if (smallPlayerFragment != null) {
                        smallPlayerFragment.init(MainActivity.this, metadata, playbackState, position);
                        smallPlayerFragment.invalidate();
                    } else {
                        if ((largePlayerFragment == null || !largePlayerFragment.isVisible())) {
                            SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
                            smallPlayerFragment.init(MainActivity.this, metadata, playbackState, position);
                            MainActivity.this.smallPlayerFragment = smallPlayerFragment;
                            showFragment(R.anim.fadeinshort, MainActivity.this.smallPlayerFragment);
                        }
                    }
                }
            }
        }
    };

    private void hideSmallPlayerFragment() {
        hideFragment(smallPlayerFragment);
        smallPlayerFragment = null;
    }

    private void showLargePlayerFragment() {
        LargePlayerFragment largePlayerFragment = new LargePlayerFragment();
        largePlayerFragment.initControls(this);
        this.largePlayerFragment = largePlayerFragment;
        showFragment(R.anim.slideup, this.largePlayerFragment);
    }

    private void hideLargePlayerFragment() {
        hideFragment(largePlayerFragment);
        largePlayerFragment = null;
    }

    private void hideTrackListFragment() {
        hideFragment(trackListFragment);
        trackListFragment = null;
    }

    private void showFragment(int animationIn, Fragment fragment) {
        if (fragment != null && !fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animationIn, R.anim.fadeoutshort)
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    private void hideFragment(Fragment fragment) {
        if (fragment != null && fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter() {
            super(MainActivity.this.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int i) {
            if (i == Constants.INDEX_FRAGMENT_SETTINGS) {
                return getSettingsPage();
            } else {
                return getTrackListFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
