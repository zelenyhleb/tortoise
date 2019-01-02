package ru.krivocraft.kbmp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.ComponentName;
import android.content.Intent;
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

import java.util.List;
import java.util.Objects;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TortoiseActivity extends AppCompatActivity implements Track.StateCallback {

    private boolean mBounded = false;

    private SmallPlayerFragment smallPlayerFragment;

    private Playlist allTracksPlaylist;

    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;

    private Service serviceInstance;

    private boolean startedByNotification = false;

    private SQLiteProcessor database;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Service.LocalBinder binder = (Service.LocalBinder) iBinder;
            serviceInstance = binder.getServerInstance();

            serviceInstance.addListener(TortoiseActivity.this);

            mBounded = true;
            showSmallPlayerFragment();

            if (startedByNotification) {
                startedByNotification = false;
            }

            loadCompositions();

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fadeinshort, R.anim.fadeoutshort)
                    .add(R.id.list_container, getTrackListFragment(allTracksPlaylist))
                    .commit();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBounded = false;
        }
    };

    @NonNull
    private TrackListPage getTrackListFragment(Playlist playlist) {
        AdapterView.OnItemClickListener onListItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Playlist.TracksAdapter adapter = (Playlist.TracksAdapter) adapterView.getAdapter();
                Track track = (Track) adapterView.getItemAtPosition(position);
                Playlist newPlaylist = adapter.getPlaylist();

                if (!newPlaylist.equals(serviceInstance.getPlaylist())) {
                    serviceInstance.getPlaylist().deselect();
                    serviceInstance.setPlaylist(newPlaylist);
                    serviceInstance.skipToNew(position);
                    serviceInstance.play();
                } else {
                    if (!track.equals(serviceInstance.getCurrentTrack())) {
                        serviceInstance.skipToNew(newPlaylist.indexOf(track));
                    } else {
                        if (serviceInstance.isPlaying()) {
                            serviceInstance.pause();
                        } else {
                            serviceInstance.play();
                        }
                    }
                }
                showSmallPlayerFragment();
            }
        };
        TrackListPage trackListPage = new TrackListPage();
        if (serviceInstance != null) {
            serviceInstance.addListener(trackListPage);
        }
        trackListPage.init(playlist, onListItemClickListener, true);
        return trackListPage;
    }

    @NonNull
    private SettingsPage getSettingsFragment() {
        SettingsPage settingsPage = new SettingsPage();
        settingsPage.setContext(this);
        return settingsPage;
    }

    private void loadCompositions() {
        if (mBounded) {
            List<Track> tracks = database.readCompositions(null, null);
            for (Track track : tracks) {
                if (!allTracksPlaylist.contains(track)) {
                    allTracksPlaylist.addComposition(track);
                    allTracksPlaylist.notifyAdapters();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tortoise);

        database = new SQLiteProcessor(this);
        allTracksPlaylist = new Playlist(this, "All Tracks");

        RelativeLayout layout = findViewById(R.id.main_layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @NonNull
    private List<Playlist> getAllCustomPlaylists() {
        return database.getPlaylists();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBounded) {
            serviceInstance.removeListener(TortoiseActivity.this);
        }
        unbindService(mConnection);
    }

    private void bindService() {
        Intent serviceIntent = new Intent(this, Service.class);
        if (!Service.isRunning()) {
            startService(serviceIntent);
        }

        bindService(serviceIntent, mConnection, BIND_ABOVE_CLIENT);
    }

    private void startSearchTask() {

        Track.OnTracksFoundListener listener = new Track.OnTracksFoundListener() {
            @Override
            public void onTrackSearchingCompleted(List<Track> tracks) {
                database.writeCompositions(tracks);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadCompositions();
                    }
                });
            }
        };

        RecursiveSearchTask searchTask = new RecursiveSearchTask();
        searchTask.execute(new SearchTaskBundle(this, listener, allTracksPlaylist));
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            startSearchTask();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        if (Objects.equals(getIntent().getAction(), Constants.ACTION_SHOW_PLAYER)) {
            startedByNotification = true;
        }

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
                    smallPlayerFragment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(TortoiseActivity.this, PlayerActivity.class));
                        }
                    });

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
