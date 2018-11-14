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
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.*;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TortoiseActivity extends AppCompatActivity implements Track.OnTrackStateChangedListener {

    private boolean mBounded = false;

    private ViewPager pager;
    private PlayerFragmentAdapter pagerAdapter;

    private AbstractTrackViewFragment trackViewFragment;
    private PlayerFragment playerFragment;
    private SmallPlayerFragment smallPlayerFragment;

    private PlaylistsAdapter mPlaylistsAdapter;

    private List<Playlist> playlists;
    private Playlist allTracksPlaylist;

    private Playlist selectedPlaylist;

    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 22892;

    private PlayerService serviceInstance;
    private FloatingActionButton addPlaylistButton;

    private boolean startedByNotification = false;

    private SQLiteProcessor database;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            serviceInstance = binder.getServerInstance();

            mPlaylistsAdapter = new PlaylistsAdapter(playlists, TortoiseActivity.this);

            pagerAdapter = new PlayerFragmentAdapter();

            pager.setAdapter(pagerAdapter);
            pager.setCurrentItem(Constants.INDEX_FRAGMENT_PLAYLISTGRID);
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == Constants.INDEX_FRAGMENT_PLAYLISTGRID) {
                        showAddButton();
                    } else {
                        hideAddButton();
                    }
                    invalidateTrackViewFragment();
                    if (position == Constants.INDEX_FRAGMENT_PLAYER) {
                        hideSmallPlayerFragment();
                    } else {
                        showSmallPlayerFragment();
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });


            serviceInstance.addListener(TortoiseActivity.this);

            mBounded = true;
            showSmallPlayerFragment();

            if (startedByNotification) {
                pager.setCurrentItem(Constants.INDEX_FRAGMENT_PLAYER);
                if (serviceInstance.isPlaying()) {
                    startPlayerFragmentUIPlaying();
                } else {
                    stopPlayerFragmentUIPlaying();
                }
                startedByNotification = false;
            }

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

    private void removeFragment(final Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    private void addFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slideup, R.anim.fadeoutshort)
                .add(R.id.container, fragment)
                .commit();
    }

    @NonNull
    private PlaylistGridFragment getPlaylistGridFragment() {
        AdapterView.OnItemClickListener onGridItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPlaylist = (Playlist) parent.getItemAtPosition(position);
                pager.setCurrentItem(Constants.INDEX_FRAGMENT_TRACKLIST);
                if (trackViewFragment instanceof TrackListFragment) {
                    TrackListFragment fragment = (TrackListFragment) trackViewFragment;
                    fragment.updateData(selectedPlaylist);
                }
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

                if (!newPlaylist.equals(serviceInstance.getCurrentPlaylist())) {
                    serviceInstance.getCurrentPlaylist().deselect();
                    serviceInstance.setCurrentPlaylist(newPlaylist);
                    serviceInstance.newComposition(newPlaylist.indexOf(track));
                    serviceInstance.start();
                } else {
                    if (!track.equals(serviceInstance.getCurrentTrack())) {
                        serviceInstance.newComposition(newPlaylist.indexOf(track));
                    } else {
                        if (serviceInstance.isPlaying()) {
                            serviceInstance.stop();
                        } else {
                            serviceInstance.start();
                        }
                    }
                }
                showSmallPlayerFragment();
            }
        };
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(playlist, onListItemClickListener);
        return trackListFragment;
    }

    @NonNull
    private SettingsFragment getSettingsFragment() {
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setContext(this);
        return settingsFragment;
    }

    private void loadCompositions() {
        if (mBounded) {
            List<Track> tracks = database.readCompositions(null, null);
            for (Track track : tracks) {
                if (!allTracksPlaylist.contains(track)) {
                    allTracksPlaylist.addComposition(track);
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tortoise);

        database = new SQLiteProcessor(this);

        allTracksPlaylist = getAllTracksPlaylist();

        playlists = new ArrayList<>();
        playlists.add(allTracksPlaylist);
        playlists.addAll(getAllCustomPlaylists());

        playlists.addAll(compilePlaylistsByAuthor());

        pager = findViewById(R.id.pager);
        if (Objects.equals(getIntent().getAction(), Constants.ACTION_SHOW_PLAYER)) {
            startedByNotification = true;
        }

        addPlaylistButton = findViewById(R.id.add_playlist_button);
        addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TortoiseActivity.this, PlaylistCreationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() != Constants.INDEX_FRAGMENT_PLAYLISTGRID) {
            pager.setCurrentItem(Constants.INDEX_FRAGMENT_PLAYLISTGRID);
        } else {
            super.onBackPressed();
        }
    }

    @NonNull
    private Playlist getAllTracksPlaylist() {
        return new Playlist(database.readCompositions(null, null), this, "All Tracks");
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
        Intent serviceIntent = new Intent(this, PlayerService.class);
        if (!PlayerService.isRunning()) {
            startService(serviceIntent);
        }

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
                bindService();
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
            bindService();
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
            if (smallPlayerFragment != null) {
                Track track = serviceInstance.getCurrentTrack();
                if (track != null) {
                    int progress = Utils.getSeconds(serviceInstance.getPlayerProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    boolean playing = serviceInstance.isPlaying();

                    smallPlayerFragment.setData(track, progress, duration, playing);
                    smallPlayerFragment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pager.setCurrentItem(Constants.INDEX_FRAGMENT_PLAYER);
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

    private int VIEWPAGER_PAGE_COUNT = 3;

    private void showSmallPlayerFragment() {
        if (mBounded) {
            if (smallPlayerFragment == null && pager.getCurrentItem() != Constants.INDEX_FRAGMENT_PLAYER) {
                Track track = serviceInstance.getCurrentTrack();
                if (track != null) {
                    createPlayerFragment();
                    smallPlayerFragment = new SmallPlayerFragment();
                    int progress = Utils.getSeconds(serviceInstance.getPlayerProgress());
                    int duration = Utils.getSeconds(Integer.parseInt(track.getDuration()));
                    smallPlayerFragment.setData(track, progress, duration, serviceInstance.isPlaying());
                    addFragment(smallPlayerFragment);
                }
            }
        }
    }

    private void createPlayerFragment() {
        if (VIEWPAGER_PAGE_COUNT != 4) {
            VIEWPAGER_PAGE_COUNT = 4;
            pagerAdapter.notifyDataSetChanged();
        }
    }

    private void hideSmallPlayerFragment() {
        if (smallPlayerFragment != null) {
            removeFragment(smallPlayerFragment);
            smallPlayerFragment = null;
        }
    }

    @Override
    public void onTrackStateChanged(Track.TrackState state) {
        switch (state) {
            case NEW_TRACK:
                refreshPlayerFragment(true);
                if (playerFragment != null) {
                    playerFragment.updateUI();
                }
            case PLAY_PAUSE_TRACK:
                if (mBounded) {
                    if (serviceInstance.isPlaying()) {
                        startPlayerFragmentUIPlaying();
                    } else {
                        stopPlayerFragmentUIPlaying();
                    }
                }
                refreshPlayerFragment(false);
                break;
        }

        invalidateTrackViewFragment();
    }

    private class PlayerFragmentAdapter extends FragmentPagerAdapter {


        PlayerFragmentAdapter() {
            super(TortoiseActivity.this.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case Constants.INDEX_FRAGMENT_SETTINGS:
                    trackViewFragment = null;
                    return getSettingsFragment();
                case Constants.INDEX_FRAGMENT_PLAYLISTGRID:
                    PlaylistGridFragment playlistGridFragment = getPlaylistGridFragment();
                    trackViewFragment = playlistGridFragment;
                    return playlistGridFragment;
                case Constants.INDEX_FRAGMENT_TRACKLIST:
                    if (selectedPlaylist == null) {
                        if (mBounded) {
                            if (serviceInstance.getCurrentPlaylist() != null) {
                                selectedPlaylist = serviceInstance.getCurrentPlaylist();
                            } else {
                                selectedPlaylist = allTracksPlaylist;
                            }
                        } else {
                            selectedPlaylist = allTracksPlaylist;
                        }
                    }
                    TrackListFragment trackListFragment = getTrackListFragment(selectedPlaylist);
                    trackViewFragment = trackListFragment;
                    return trackListFragment;
                case Constants.INDEX_FRAGMENT_PLAYER:
                    PlayerFragment playerFragment = getPlayerFragment();
                    TortoiseActivity.this.playerFragment = playerFragment;
                    return playerFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return VIEWPAGER_PAGE_COUNT;
        }
    }

    @NonNull
    private PlayerFragment getPlayerFragment() {
        PlayerFragment playerFragment = new PlayerFragment();
        playerFragment.setContext(TortoiseActivity.this);
        playerFragment.setServiceInstance(serviceInstance);
        return playerFragment;
    }

    private Timer compositionProgressTimer;

    private void startPlayerFragmentUIPlaying() {
        if (compositionProgressTimer == null) {
            compositionProgressTimer = new Timer();
            compositionProgressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (playerFragment != null) {
                                playerFragment.updateBar();
                                playerFragment.startUIPlaying();
                            }
                        }
                    });
                }
            }, Constants.ONE_SECOND, Constants.ONE_SECOND);
        }
    }

    private void stopPlayerFragmentUIPlaying() {
        if (compositionProgressTimer != null) {
            compositionProgressTimer.cancel();
            compositionProgressTimer = null;
        }
        if (playerFragment != null) {
            playerFragment.stopUIPlaying();
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
