/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.contexts;

import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.ColorManager;
import ru.krivocraft.tortoise.core.OldStuffCollector;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;
import ru.krivocraft.tortoise.fragments.BaseFragment;
import ru.krivocraft.tortoise.fragments.SettingsFragment;
import ru.krivocraft.tortoise.fragments.TrackEditorFragment;
import ru.krivocraft.tortoise.fragments.explorer.Explorer;
import ru.krivocraft.tortoise.fragments.explorer.ExplorerFragment;
import ru.krivocraft.tortoise.fragments.player.PlayerController;
import ru.krivocraft.tortoise.fragments.player.SmallPlayerFragment;
import ru.krivocraft.tortoise.fragments.tracklist.TrackListFragment;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends BaseActivity {


    private SmallPlayerFragment smallPlayerFragment;
    private MenuItem settingsButton;

    public static final String ACTION_HIDE_PLAYER = "action_hide_player";
    public static final String ACTION_SHOW_PLAYER = "action_show_player";
    public static final String ACTION_SHOW_TRACK_EDITOR = "action_show_tracks_editor";

    private BroadcastReceiver showPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SHOW_PLAYER.equals(intent.getAction())) {
                showSmallPlayerFragment();
            } else if (ACTION_HIDE_PLAYER.equals(intent.getAction())) {
                hideFragment(smallPlayerFragment);
            }
        }
    };

    private BroadcastReceiver showEditorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showTrackEditorFragment(TrackReference.fromJson(intent.getStringExtra(Track.EXTRA_TRACK)));
        }
    };

    private BaseFragment currentFragment;
    private Explorer explorer;
    private TracksStorageManager tracksStorageManager;
    private ColorManager colorManager;

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        showSmallPlayerFragment();
        if (smallPlayerFragment != null)
            smallPlayerFragment.updateMediaMetadata(newMetadata);
        recolorInterface(newMetadata);
    }

    private void recolorInterface(MediaMetadataCompat newMetadata) {
        if (currentFragment instanceof TrackListFragment) {
            TrackListFragment currentFragment = (TrackListFragment) this.currentFragment;
            Track track = tracksStorageManager.getTrack(tracksStorageManager.getReference(newMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            currentFragment.notifyTracksStateChanged();
            currentFragment.changeColor(colorManager.getColorResource(track.getColor()));
        }
        if (currentFragment instanceof ExplorerFragment) {
            ExplorerFragment fragment = (ExplorerFragment) this.currentFragment;
            Track track = tracksStorageManager.getTrack(tracksStorageManager.getReference(newMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            fragment.changeColor(colorManager.getColorResource(track.getColor()));
        }
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        showSmallPlayerFragment();
        if (smallPlayerFragment != null)
            smallPlayerFragment.updatePlaybackState(newPlaybackState);
        if (currentFragment instanceof TrackListFragment) {
            ((TrackListFragment) currentFragment).notifyTracksStateChanged();
        }
    }

    @Override
    void onMediaBrowserConnected() {
        showSmallPlayerFragment();
        recolorInterface(mediaController.getMetadata());
    }

    private TrackListFragment getTrackListFragment(TrackList trackList) {
        TrackListFragment trackListFragment = TrackListFragment.newInstance();
        if (trackList != null) {
            trackListFragment.setTitle(trackList.getDisplayName());
            trackListFragment.setTrackList(trackList);
            try {
                Track track = tracksStorageManager.getTrack(tracksStorageManager.getReference(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
                trackListFragment.setTintColor(colorManager.getColorResource(track.getColor()));
            } catch (NullPointerException e) {
                //Metadata is null
                trackListFragment.setTintColor(R.color.green700);
            }
            trackListFragment.setShowControls(true);
        }
        return trackListFragment;
    }

    private ExplorerFragment getExplorerFragment() {
        ExplorerFragment explorerFragment = ExplorerFragment.newInstance();
        explorerFragment.setExplorer(explorer);
        explorerFragment.setListener(this::showTrackListFragment);
        try {
            Track track = tracksStorageManager.getTrack(tracksStorageManager.getReference(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            explorerFragment.setTintColor(colorManager.getColorResource(track.getColor()));
        } catch (NullPointerException e) {
            //Metadata is null
            explorerFragment.setTintColor(R.color.green700);
        }
        return explorerFragment;
    }

    private TrackEditorFragment getTrackEditorFragment(TrackReference reference) {
        return TrackEditorFragment.newInstance(this::showExplorerFragment, reference);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearOld();
    }

    private void clearOld() {
        OldStuffCollector collector = new OldStuffCollector(this);
        collector.execute();
    }

    @Override
    void init() {
        setContentView(R.layout.activity_tortoise);
        configureLayoutTransition();

        startService();
        registerPlayerControlReceiver();

        explorer = new Explorer(this::invalidate, this);
        tracksStorageManager = new TracksStorageManager(this);
        colorManager = new ColorManager(this);

        IntentFilter filter = new IntentFilter(ACTION_SHOW_TRACK_EDITOR);
        registerReceiver(showEditorReceiver, filter);

        if (currentFragment != null) {
            restoreFragment();
        } else {
            showExplorerFragment();
        }
    }

    private void configureLayoutTransition() {
        RelativeLayout layout = findViewById(R.id.main_layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    private void startService() {
        if (!AndroidMediaService.running) {
            startService(new Intent(this, AndroidMediaService.class));
        }
    }

    private void registerPlayerControlReceiver() {
        IntentFilter showPlayerFilter = new IntentFilter();
        showPlayerFilter.addAction(ACTION_SHOW_PLAYER);
        showPlayerFilter.addAction(ACTION_HIDE_PLAYER);
        registerReceiver(showPlayerReceiver, showPlayerFilter);
    }

    private void invalidate() {
        if (currentFragment != null) {
            runOnUiThread(currentFragment::invalidate);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        this.settingsButton = menu.findItem(R.id.action_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showSettingsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExplorerFragment() {
        replaceFragment(getExplorerFragment());
        showSmallPlayerFragment();
    }

    private void showTrackListFragment(TrackList trackList) {
        replaceFragment(getTrackListFragment(trackList));
        showSmallPlayerFragment();
    }

    private void showSettingsFragment() {
        replaceFragment(SettingsFragment.newInstance());
        hideFragment(smallPlayerFragment);
    }

    private void showTrackEditorFragment(TrackReference trackReference) {
        replaceFragment(getTrackEditorFragment(trackReference));
        hideFragment(smallPlayerFragment);
    }

    private void replaceFragment(BaseFragment fragment) {
        addFragment(fragment);
        redrawActionBar(fragment);
    }

    private void redrawActionBar(BaseFragment fragment) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(fragment.getTitle());
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof ExplorerFragment) {
            super.onBackPressed();
        } else {
            if (currentFragment instanceof SettingsFragment) {
                showSmallPlayerFragment();
                showExplorerFragment();
            } else if (currentFragment instanceof TrackListFragment) {
                showExplorerFragment();
            } else if (currentFragment instanceof TrackEditorFragment) {
                ((TrackEditorFragment) currentFragment).onBackPressed();
            }
        }
        onFragmentChanged();
    }

    private void onFragmentChanged() {
        redrawActionBar(currentFragment);
        if (settingsButton != null) {
            if (currentFragment instanceof SettingsFragment) {
                settingsButton.setVisible(false);
            } else {
                settingsButton.setVisible(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(showEditorReceiver);
        unregisterReceiver(showPlayerReceiver);
        super.onDestroy();
    }

    private void showSmallPlayerFragment() {
        if (mediaController != null && mediaController.getMetadata() != null
                && (smallPlayerFragment == null || !smallPlayerFragment.isVisible())
                && mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_STOPPED) {
            SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
            smallPlayerFragment.setController(new PlayerController() {
                @Override
                public void onPlay() {
                    mediaController.getTransportControls().play();
                }

                @Override
                public void onPause() {
                    mediaController.getTransportControls().pause();
                }

                @Override
                public void onNext() {
                    mediaController.getTransportControls().skipToNext();
                }

                @Override
                public void onPrevious() {
                    mediaController.getTransportControls().skipToPrevious();
                }

                @Override
                public void onSeekTo(int position) {
                    mediaController.getTransportControls().seekTo(position);
                }
            });
            smallPlayerFragment.setInitialData(mediaController.getMetadata(), mediaController.getPlaybackState());
            MainActivity.this.smallPlayerFragment = smallPlayerFragment;
            showFragment(smallPlayerFragment);
        }
    }

    private void restoreFragment() {
        if (currentFragment instanceof TrackListFragment) {
            currentFragment = getTrackListFragment(((TrackListFragment) currentFragment).getTrackList());
        }

        if (currentFragment instanceof ExplorerFragment) {
            currentFragment = getExplorerFragment();
        }

        if (currentFragment instanceof SettingsFragment) {
            currentFragment = SettingsFragment.newInstance();
        }
        addFragment(currentFragment);
    }

    private void addFragment(BaseFragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction transaction = fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fadeinshort, R.anim.fadeoutshort);

            transaction.replace(R.id.fragment_container, fragment);

            transaction.commitNow();

            this.currentFragment = fragment;
            onFragmentChanged();
        }
    }

    private void showFragment(BaseFragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slideup, R.anim.fadeoutshort);

            transaction.replace(R.id.player_container, fragment);

            transaction.commitNowAllowingStateLoss();
        }
    }

    private void hideFragment(Fragment fragment) {
        if (fragment != null && fragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitNowAllowingStateLoss();
        }
    }

}
