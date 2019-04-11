package ru.krivocraft.kbmp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Objects;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TortoiseActivity extends AppCompatActivity implements StateCallback {

    private boolean mBounded = false;

    private SmallPlayerFragment smallPlayerFragment;
    private LargePlayerFragment largePlayerFragment;

    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;

    private Service serviceInstance;

    private boolean startedByNotification = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Service.LocalBinder binder = (Service.LocalBinder) iBinder;
            serviceInstance = binder.getServerInstance();

            serviceInstance.addStateCallbackListener(TortoiseActivity.this);

            mBounded = true;
            refreshSmallPlayerFragment(true);

            if (startedByNotification) {
                startedByNotification = false;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBounded = false;
        }
    };

    private BroadcastReceiver trackListUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_UPDATE_TRACKLIST.equals(intent.getAction())) {
                showTrackListFragment();
            }
        }
    };

    private BroadcastReceiver showPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_SHOW_PLAYER.equals(intent.getAction())) {
                hideSmallPlayerFragment();
                hideTrackListFragment();
                showLargePlayerFragment();
            }
        }
    };
    private TrackListPage trackListFragment;

    @NonNull
    private TrackListPage getTrackListFragment(TrackList trackList) {
        AdapterView.OnItemClickListener onListItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TrackList.TracksAdapter adapter = (TrackList.TracksAdapter) adapterView.getAdapter();
                Track track = (Track) adapterView.getItemAtPosition(position);
                TrackList newTrackList = adapter.getPlaylist();

                if (!newTrackList.equals(serviceInstance.getPlaylist())) {
                    serviceInstance.getPlaylist().deselect();
                    serviceInstance.setPlaylist(newTrackList);
                    serviceInstance.skipToNew(position);
                    serviceInstance.play();
                } else {
                    if (!track.equals(serviceInstance.getCurrentTrack())) {
                        serviceInstance.skipToNew(newTrackList.indexOf(track));
                    } else {
                        if (serviceInstance.isPlaying()) {
                            serviceInstance.pause();
                        } else {
                            serviceInstance.play();
                        }
                    }
                }
                refreshSmallPlayerFragment(true);
            }
        };
        TrackListPage trackListPage = new TrackListPage();
        if (serviceInstance != null) {
            serviceInstance.addStateCallbackListener(trackListPage);
        }
        trackListPage.init(trackList, onListItemClickListener, true);
        return trackListPage;
    }

    private LargePlayerFragment getLargePlayerFragment() {
        LargePlayerFragment playerFragment = new LargePlayerFragment();
        playerFragment.setContext(this);
        playerFragment.setServiceInstance(serviceInstance);
        return playerFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tortoise);

        RelativeLayout layout = findViewById(R.id.main_layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        IntentFilter trackListUpdateFilter = new IntentFilter();
        trackListUpdateFilter.addAction(Constants.ACTION_UPDATE_TRACKLIST);
        registerReceiver(trackListUpdateReceiver, trackListUpdateFilter);

        IntentFilter showPlayerFilter = new IntentFilter();
        showPlayerFilter.addAction(Constants.ACTION_SHOW_PLAYER);
        registerReceiver(showPlayerReceiver, showPlayerFilter);
    }

    @Override
    public void onBackPressed() {
        if (largePlayerFragment != null) {
            hideLargePlayerFragment();
            showSmallPlayerFragment();
            showTrackListFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBounded) {
            serviceInstance.removeStateCallbackListener(TortoiseActivity.this);
        }
        unbindService(mConnection);
        unregisterReceiver(trackListUpdateReceiver);
    }

    private void bindService() {
        Intent serviceIntent = new Intent(this, Service.class);
        if (!Service.isRunning()) {
            startService(serviceIntent);
        }

        bindService(serviceIntent, mConnection, BIND_ABOVE_CLIENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindService();
            } else {
                Toast.makeText(this, "App needs external storage permission to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBounded) {
            bindService();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        if (Objects.equals(getIntent().getAction(), Constants.ACTION_SHOW_PLAYER)) {
            startedByNotification = true;
        }

        refreshSmallPlayerFragment(false);
    }

    private void refreshSmallPlayerFragment(boolean newDataAvailable) {
        if (mBounded) {
            if (smallPlayerFragment != null) {
                Track track = serviceInstance.getCurrentTrack();
                if (track != null) {
                    int progress = Utils.getSeconds(serviceInstance.getProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    boolean playing = serviceInstance.isPlaying();

                    smallPlayerFragment.setData(track, progress, duration, playing);

                    if (newDataAvailable) {
                        smallPlayerFragment.initStaticUI();
                    }
                    smallPlayerFragment.initNonStaticUI();
                }
            } else {
                showSmallPlayerFragment();
            }
        }
    }

    private void showSmallPlayerFragment() {
        if (largePlayerFragment == null || !largePlayerFragment.isVisible()) {
            Track track = serviceInstance.getCurrentTrack();
            if (track != null) {
                smallPlayerFragment = new SmallPlayerFragment();
                int progress = Utils.getSeconds(serviceInstance.getProgress());
                int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                smallPlayerFragment.setData(track, progress, duration, serviceInstance.isPlaying());
                showFragment(R.anim.fadeinshort, R.anim.fadeoutshort, R.id.container, smallPlayerFragment);
            }
        }
    }

    private void hideSmallPlayerFragment() {
        hideFragment(smallPlayerFragment);
        smallPlayerFragment = null;
    }

    private void showLargePlayerFragment() {
        largePlayerFragment = getLargePlayerFragment();
        showFragment(R.anim.slideup, R.anim.fadeoutshort, R.id.list_container, largePlayerFragment);
    }

    private void hideLargePlayerFragment() {
        hideFragment(largePlayerFragment);
        largePlayerFragment = null;
    }

    private void showTrackListFragment() {
        trackListFragment = getTrackListFragment(serviceInstance.getPlaylist());
        showFragment(R.anim.fadeinshort, R.anim.fadeoutshort, R.id.list_container, trackListFragment);
    }

    private void hideTrackListFragment() {
        hideFragment(trackListFragment);
        trackListFragment = null;
    }

    private void showFragment(int animation1, int animation2, int container, Fragment fragment) {
        if (mBounded) {
            if (fragment != null && !fragment.isVisible()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(animation1, animation2)
                        .add(container, fragment)
                        .commit();
            }
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

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        refreshSmallPlayerFragment(true);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        refreshSmallPlayerFragment(false);
    }


}
