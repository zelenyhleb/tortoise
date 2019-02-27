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

            serviceInstance.getTrackProvider().search();

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
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fadeinshort, R.anim.fadeoutshort)
                        .add(R.id.list_container, getTrackListFragment(serviceInstance.getPlaylist()))
                        .commit();
            }
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tortoise);

        RelativeLayout layout = findViewById(R.id.main_layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_UPDATE_TRACKLIST);
        registerReceiver(trackListUpdateReceiver, intentFilter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        if (mBounded) {
            if (smallPlayerFragment == null) {
                Track track = serviceInstance.getCurrentTrack();
                if (track != null) {
                    smallPlayerFragment = new SmallPlayerFragment();
                    int progress = Utils.getSeconds(serviceInstance.getProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    smallPlayerFragment.setData(track, progress, duration, serviceInstance.isPlaying());
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slideup, R.anim.fadeoutshort)
                            .add(R.id.container, smallPlayerFragment)
                            .commit();
                }
            }
        }
    }

    private void hideSmallPlayerFragment() {
        if (smallPlayerFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(smallPlayerFragment)
                    .commit();
            smallPlayerFragment = null;
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
