package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity implements Track.OnTrackStateChangedListener {

    private boolean mBounded = false;

    private PlaylistAdapter mPlaylistAdapter;
    private PlayerService mService;

    private SQLiteProcessor database;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBounded = true;

            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getServerInstance();
            mService.addListener(PlaylistActivity.this);

            ListView playlistView = findViewById(R.id.playlist);

            mPlaylistAdapter = new PlaylistAdapter(mService.getCurrentPlaylist(), PlaylistActivity.this);
            playlistView.setAdapter(mPlaylistAdapter);

            playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Track track = (Track) adapterView.getItemAtPosition(i);

                    if (!track.equals(mService.getCurrentTrack())) {
                        mService.newComposition(mService.getCurrentPlaylist().indexOf(track));
                    }

                    Intent intent = new Intent(PlaylistActivity.this, PlayerActivity.class);
                    startActivity(intent);
                }
            });

            refreshFragment();
            loadCompositions();
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
            for (Track track : database.readCompositions()) {
                System.out.println(track.getIdentifier() + ":" + track.getArtist() + ":" + track.getName() + ":" + track.getPath());
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

    private void initPlaylist() {
        database = new SQLiteProcessor(this);

        RecursiveSearchTask searchTask = new RecursiveSearchTask();
        searchTask.execute(Environment.getExternalStorageDirectory());

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

    private Track.OnTrackFoundListener onTrackFoundListener = new Track.OnTrackFoundListener() {
        @Override
        public void onTrackFound(Track track) {
            database.writeComposition(track);
            loadCompositions();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlaylistAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        initPlaylist();
        refreshFragment();
    }

    private void refreshFragment() {
        hideFragment();
        showFragment();
    }

    private void showFragment() {
        if (mBounded) {
            Track track = mService.getCurrentTrack();
            if (track != null) {
                fragment = new PlayerFragment();
                int progress = Utils.getSeconds(mService.getProgress());
                int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                fragment.setData(track.getArtist(), track.getName(), progress, duration, mService.isPlaying());
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, fragment)
                        .commitAllowingStateLoss();
            }
        }
    }

    private void hideFragment() {
        if (fragment != null) {
            fragment.destroy();
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
            fragment = null;
        }
    }

    public void onClick(View v) {
        startActivity(new Intent(PlaylistActivity.this, SettingsActivity.class));
    }

    @Override
    public void onNewTrackState() {
        refreshFragment();
    }

    class RecursiveSearchTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... files) {
            Utils.searchRecursively(files[0], onTrackFoundListener);
            return null;
        }
    }

}
