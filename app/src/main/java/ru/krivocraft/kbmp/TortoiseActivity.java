package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.util.*;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TortoiseActivity extends AppCompatActivity implements Track.OnTrackStateChangedListener {

    private boolean mBounded = false;

    private enum FragmentState {
        PLAYLISTS_GRID,
        TRACKS_LIST
    }

    private FragmentState fragmentState = FragmentState.TRACKS_LIST;

    private AbstractTrackViewFragment trackViewFragment;
    private PlayerFragment playerFragment;

    private Playlist.TracksAdapter mTracksAdapter;
    private PlaylistsAdapter mPlaylistsAdapter;

    private List<Playlist> playlists;
    private Playlist allTracksPlaylist;

    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;

    private PlayerService mService;

    private FloatingActionButton addPlaylistButton;

    private SQLiteProcessor database;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getServerInstance();

            mTracksAdapter = mService.getCurrentPlaylist().getTracksAdapter();
            mPlaylistsAdapter = new PlaylistsAdapter(playlists, TortoiseActivity.this);

            ViewPager pager = findViewById(R.id.pager);
            pager.setAdapter(new PlayerFragmentAdapter());
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        showAddButton();
                    } else {
                        hideAddButton();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            mService.addListener(TortoiseActivity.this);

            mBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBounded = false;
        }
    };

    private void showAddButton() {
        if (addPlaylistButton.getVisibility() == View.INVISIBLE) {
            addPlaylistButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadeinshort));
            addPlaylistButton.setVisibility(View.VISIBLE);
        }
    }

    private void hideAddButton() {
        if (addPlaylistButton.getVisibility() == View.VISIBLE) {
            addPlaylistButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadeoutshort));
            addPlaylistButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    private void showPlayerFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fadeinshort, R.anim.fadeoutshort)
                .add(R.id.container, fragment)
                .commit();
    }

    @NonNull
    private PlaylistGridFragment getPlaylistGridFragment() {
        AdapterView.OnItemClickListener onGridItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TODO: MOVE PAGER TO SELECTED PLAYLIST
                hideAddButton();
            }
        };
        AdapterView.OnItemLongClickListener onGridItemLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                showPlaylistDeletionDialog(parent, position);
                return true;
            }
        };
        PlaylistGridFragment playlistGridFragment = new PlaylistGridFragment();
        playlistGridFragment.setData(mPlaylistsAdapter, onGridItemClickListener, onGridItemLongClickListener);
        return playlistGridFragment;
    }

    private void showPlaylistDeletionDialog(final AdapterView<?> parent, final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TortoiseActivity.this);
        alertDialogBuilder.setIcon(R.drawable.ic_launcher);
        alertDialogBuilder.setTitle("Are you sure want to delete this playlist?");
        alertDialogBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Playlist playlist = (Playlist) parent.getItemAtPosition(position);
                database.deletePlaylist(playlist.getName());
                invalidateTrackViewFragment();
            }
        });
        alertDialogBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    @NonNull
    private TrackListFragment getTrackListFragment(Playlist playlist) {
        AdapterView.OnItemClickListener onListItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Playlist.TracksAdapter adapter = (Playlist.TracksAdapter) adapterView.getAdapter();
                Track track = (Track) adapterView.getItemAtPosition(i);
                Playlist newPlaylist = adapter.getPlaylist();

                if (!newPlaylist.equals(mService.getCurrentPlaylist())) {
                    mService.setCurrentPlaylist(newPlaylist);
                    mService.newComposition(newPlaylist.indexOf(track));
                    mService.start();
                } else {
                    if (!track.equals(mService.getCurrentTrack())) {
                        mService.newComposition(newPlaylist.indexOf(track));
                    } else {
                        if (mService.isPlaying()) {
                            mService.stop();
                        } else {
                            mService.start();
                        }
                    }
                }
                showPlayerFragment();
            }
        };
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.setData(playlist, onListItemClickListener);
        return trackListFragment;
    }

    private void loadCompositions() {
        if (mBounded) {
            Playlist playlist = mService.getCurrentPlaylist();
            List<Track> tracks = database.readCompositions(null, null);
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

        database = new SQLiteProcessor(this);

        playlists = getAllCustomPlaylists();
        allTracksPlaylist = getAllTracksPlaylist();

        playlists.addAll(compilePlaylistsByAuthor());

        addPlaylistButton = findViewById(R.id.add_playlist_button);
        addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TortoiseActivity.this, PlaylistCreationActivity.class);
                startActivity(intent);
            }
        });
    }

    @NonNull
    private Playlist getAllTracksPlaylist() {
        return new Playlist(database.readCompositions(null, null), this);
    }

    @NonNull
    private List<Playlist> getAllCustomPlaylists() {
        return database.getPlaylists();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBounded) {
            mService.removeListener(TortoiseActivity.this);
        }
        unbindService(mConnection);
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        startService(serviceIntent);

        bindService(serviceIntent, mConnection, BIND_ABOVE_CLIENT);
    }

    private void startSearchTask() {
        RecursiveSearchTask searchTask = new RecursiveSearchTask();
        searchTask.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                Toast.makeText(this, "App needs external storage permission to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    private List<Playlist> compilePlaylistsByAuthor() {
        Map<String, Playlist> playlistMap = new HashMap<>();
        for (Track track : allTracksPlaylist.getTracks()) {
            Playlist playlist = playlistMap.get(track.getArtist());
            if (playlist == null) {
                playlist = new Playlist(this, track.getArtist());
                playlistMap.put(track.getArtist(), playlist);
            }
            if (!playlist.contains(track)) {
                playlist.addComposition(track);
            }
        }
        return new ArrayList<>(playlistMap.values());
    }

    private Track.OnTracksFoundListener onTracksFoundListener = new Track.OnTracksFoundListener() {
        @Override
        public void onTrackSearchingCompleted(List<Track> tracks) {
            database.writeCompositions(tracks);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadCompositions();
                    invalidateTrackViewFragment();
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBounded) {
            startService();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            startSearchTask();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        invalidateTrackViewFragment();
    }

    private void invalidateTrackViewFragment() {
        if (trackViewFragment != null) {
            trackViewFragment.invalidate();
        }
    }

    private void refreshPlayerFragment(boolean newDataAvailable) {
        if (mBounded) {
            if (playerFragment != null) {
                Track track = mService.getCurrentTrack();
                if (track != null) {
                    int progress = Utils.getSeconds(mService.getPlayerProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    boolean playing = mService.isPlaying();

                    playerFragment.setData(track, progress, duration, playing);

                    if (newDataAvailable) {
                        playerFragment.initStaticUI();
                    }
                    playerFragment.initNonStaticUI();
                }
            } else {
                showPlayerFragment();
            }
        }
    }

    private void showPlayerFragment() {
        if (mBounded) {
            if (playerFragment == null) {
                Track track = mService.getCurrentTrack();
                if (track != null) {
                    playerFragment = new PlayerFragment();
                    int progress = Utils.getSeconds(mService.getPlayerProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    playerFragment.setData(track, progress, duration, mService.isPlaying());
                    showPlayerFragment(playerFragment);
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings:
                buttonSettings();
                break;
        }
    }

    private void buttonSettings() {
        startActivity(new Intent(TortoiseActivity.this, SettingsActivity.class));
    }

    @Override
    public void onTrackStateChanged(Track.TrackState state) {
        switch (state) {
            case NEW_TRACK:
                refreshPlayerFragment(true);
            case PLAY_PAUSE_TRACK:
                refreshPlayerFragment(false);
        }
        invalidateTrackViewFragment();
    }

    private class PlayerFragmentAdapter extends FragmentPagerAdapter {

        private int PAGE_COUNT = 2;

        PlayerFragmentAdapter() {
            super(TortoiseActivity.this.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getPlaylistGridFragment();
                case 1:
                    return getTrackListFragment(mService.getCurrentPlaylist());
            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }

    class RecursiveSearchTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Utils.search(TortoiseActivity.this, onTracksFoundListener);
            return null;
        }
    }

}
