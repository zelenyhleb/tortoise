package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity implements Track.OnTrackStateChangedListener {

    private boolean mBounded = false;

    private Playlist.TracksAdapter mTracksAdapter;
    private PlayerService mService;

    private SQLiteProcessor database;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getServerInstance();

            mService.addListener(PlaylistActivity.this);

            mTracksAdapter = new Playlist.TracksAdapter(mService.getCurrentPlaylist(), PlaylistActivity.this);
            AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Track track = (Track) adapterView.getItemAtPosition(i);
                    if (!track.equals(mService.getCurrentTrack())) {
                        mService.newComposition(mService.getCurrentPlaylist().indexOf(track));
                    } else {
                        if (mService.isPlaying()) {
                            mService.stop();
                        } else {
                            mService.start();
                        }
                    }
                    showFragment();
                }
            };

            TrackListFragment fragment = new TrackListFragment();
            fragment.setData(mTracksAdapter, onItemClickListener);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.playlist, fragment)
                    .commitAllowingStateLoss();

            mBounded = true;
            showFragment();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBounded = false;
        }
    };
    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    private PlayerFragment fragment;

    private void loadCompositions() {
        if (mBounded) {
            Playlist playlist = mService.getCurrentPlaylist();
            List<Track> tracks = database.readCompositions();
            for (Track track : tracks) {
                if (!playlist.contains(track)) {
                    playlist.addComposition(track);
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBounded) {
            mService.removeListener(PlaylistActivity.this);
        }
        unbindService(mConnection);
    }

    private void initPlaylist() {
        database = new SQLiteProcessor(this);

        RecursiveSearchTask searchTask = new RecursiveSearchTask();
        searchTask.execute();

        Intent serviceIntent = new Intent(this, PlayerService.class);
        startService(serviceIntent);

        bindService(serviceIntent, mConnection, BIND_ABOVE_CLIENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initPlaylist();
            } else {
                Toast.makeText(this, "App needs external storage permission to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Track.OnTracksFoundListener onTracksFoundListener = new Track.OnTracksFoundListener() {
        @Override
        public void onTrackSearchingCompleted(List<Track> tracks) {
            database.writeCompositions(tracks);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadCompositions();
                    mTracksAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBounded) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                initPlaylist();
            }
        } else {
            if (mService.getCurrentPlaylist().isEmpty()) {
                initPlaylist();
            }
        }
    }

    private void refreshFragment(boolean newDataAvailable) {
        if (mBounded) {
            if (fragment != null) {
                Track track = mService.getCurrentTrack();
                if (track != null) {
                    int progress = Utils.getSeconds(mService.getProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    boolean playing = mService.isPlaying();

                    fragment.setData(track, progress, duration, playing);

                    if (newDataAvailable) {
                        fragment.initStaticUI();
                    }
                    fragment.initNonStaticUI();
                }
            }
        }
    }

    private void showFragment() {
        if (mBounded) {
            if (fragment == null) {
                Track track = mService.getCurrentTrack();
                if (track != null) {
                    fragment = new PlayerFragment();
                    int progress = Utils.getSeconds(mService.getProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    fragment.setData(track, progress, duration, mService.isPlaying());
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.container, fragment)
                            .commitAllowingStateLoss();
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings:
                startActivity(new Intent(PlaylistActivity.this, SettingsActivity.class));
                break;
            case R.id.shuffle:
                if (mBounded) {
                    mService.getCurrentPlaylist().shuffle();
                    mTracksAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onTrackStateChanged(Track.TrackState state) {
        switch (state) {
            case NEW_TRACK:
                refreshFragment(true);
            case PLAY_PAUSE_TRACK:
                refreshFragment(false);
        }
    }

    class RecursiveSearchTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Utils.search(PlaylistActivity.this, onTracksFoundListener);
            return null;
        }
    }

}
