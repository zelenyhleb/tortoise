package ru.krivocraft.kbmp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {


    private SmallPlayerFragment smallPlayerFragment;
    private LargePlayerFragment largePlayerFragment;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaControllerCompat;
    private TrackList trackList;

    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;


    private BroadcastReceiver trackListUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_UPDATE_TRACKLIST.equals(intent.getAction())) {
                MainActivity.this.trackList = (TrackList) intent.getSerializableExtra("tracklist_extra");
//                hideTrackListFragment();
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
    private TrackListPage getTrackListFragment(final TrackList trackList) {
        AdapterView.OnItemClickListener onListItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TracksAdapter adapter = (TracksAdapter) adapterView.getAdapter();
                Track track = (Track) adapterView.getItemAtPosition(position);
                TrackList newTrackList = adapter.getPlaylist();
                if (mediaControllerCompat.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) == track.getIdentifier()) {
                    if (mediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        mediaControllerCompat.getTransportControls().pause();
                    } else {
                        mediaControllerCompat.getTransportControls().play();
                    }
                } else {
                    mediaControllerCompat.getTransportControls().skipToQueueItem(trackList.indexOf(track));
                }

                showSmallPlayerFragment();
            }
        };
        TrackListPage trackListPage = new TrackListPage();
        trackListPage.init(trackList, onListItemClickListener, true, MainActivity.this.getApplicationContext());
        return trackListPage;
    }

    private SmallPlayerFragment getSmallPlayerFragment() {
        SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
        smallPlayerFragment.init(MainActivity.this);

        return smallPlayerFragment;
    }

    private LargePlayerFragment getLargePlayerFragment() {
        LargePlayerFragment largePlayerFragment = new LargePlayerFragment();
        largePlayerFragment.initControls(MainActivity.this);

        largePlayerFragment.setInitialData();

        return largePlayerFragment;
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
            showTrackListFragment();
        } else {
            super.onBackPressed();
        }
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
//                bindService();
            } else {
                Toast.makeText(this, "App needs external storage permission to work", Toast.LENGTH_LONG).show();
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
        if ((largePlayerFragment == null || !largePlayerFragment.isVisible()) && smallPlayerFragment == null) {
            if (mediaControllerCompat != null && mediaControllerCompat.getMetadata() != null) {
                smallPlayerFragment = getSmallPlayerFragment();
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
        trackListFragment = getTrackListFragment(trackList);
        showFragment(R.anim.fadeinshort, R.anim.fadeoutshort, R.id.list_container, trackListFragment);
    }

    private void hideTrackListFragment() {
        hideFragment(trackListFragment);
        trackListFragment = null;
    }

    private void showFragment(int animation1, int animation2, int container, Fragment fragment) {
        if (fragment != null && !fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animation1, animation2)
                    .add(container, fragment)
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

}
