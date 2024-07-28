/*
 * Copyright (c) 2020 Nikifor Fedorov
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
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.android;

import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Optional;

import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.android.editors.TrackEditorActivity;
import ru.krivocraft.tortoise.android.explorer.Explorer;
import ru.krivocraft.tortoise.android.explorer.ExplorerFragment;
import ru.krivocraft.tortoise.android.player.ActualStamp;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.android.player.AndroidMediaService;
import ru.krivocraft.tortoise.android.player.views.PlayerHostFragment;
import ru.krivocraft.tortoise.android.player.views.SmallPlayerFragment;
import ru.krivocraft.tortoise.android.settings.SettingsFragment;
import ru.krivocraft.tortoise.android.tracklist.TrackListFragment;
import ru.krivocraft.tortoise.android.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.android.thumbnail.Colors;

public class MainActivity extends BaseActivity {

    private SmallPlayerFragment smallPlayerFragment;

    private MenuItem settingsButton;

    public static final String ACTION_HIDE_PLAYER = "action_hide_player";
    public static final String ACTION_SHOW_PLAYER = "action_show_player";
    public static final String ACTION_SHOW_TRACK_EDITOR = "action_show_tracks_editor";

    private final BroadcastReceiver showPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SHOW_PLAYER.equals(intent.getAction())) {
                showSmallPlayerFragment();
            } else if (ACTION_HIDE_PLAYER.equals(intent.getAction())) {
                hideFragment(smallPlayerFragment);
            }
        }
    };

    private final BroadcastReceiver showEditorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showTrackEditorFragment(Track.Reference.fromJson(intent.getStringExtra(Track.EXTRA_TRACK)));
        }
    };

    private BaseFragment currentFragment;
    private Explorer explorer;
    private TracksStorageManager tracksStorageManager;
    private Colors colors;

    @Override
    public void onMetadataChanged(MediaMetadataCompat newMetadata) {
        if (newMetadata == null) {
            return;
        }
        showSmallPlayerFragment();
        if (smallPlayerFragment != null)
            smallPlayerFragment.updateMediaMetadata(newMetadata);
        if (currentFragment != null)
            currentFragment.onMetadataChanged(newMetadata);
        recolorInterface(newMetadata);
    }

    private void recolorInterface(MediaMetadataCompat newMetadata) {
        String path = newMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        Track.Reference reference = tracksStorageManager.getReference(path);
        Track track = tracksStorageManager.getTrack(reference);
        currentFragment.changeColors(colors.getColorResource(track.getColor()));
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        showSmallPlayerFragment();
        if (smallPlayerFragment != null)
            smallPlayerFragment.updatePlaybackState(newPlaybackState);
        if (currentFragment != null)
            currentFragment.onPlaybackStateChanged(newPlaybackState);
        if (currentFragment instanceof TrackListFragment) {
            ((TrackListFragment) currentFragment).notifyTracksStateChanged();
        }
    }

    @Override
    public void onMediaBrowserConnected() {
        if (getIntent().getBooleanExtra(ACTION_SHOW_PLAYER, false)) {
            showPlayerHostFragment();
        } else {
            if (currentFragment != null) {
                restoreFragment();
            } else {
                showExplorerFragment();
            }
        }
        showSmallPlayerFragment();
        Optional.ofNullable(mediaController.getMetadata()).ifPresent(this::recolorInterface);
    }

    private PlayerHostFragment getPlayerHostFragment() {
        PlayerHostFragment playerHostFragment = PlayerHostFragment.newInstance();
        playerHostFragment.setInitialData(TrackList.EMPTY, new AndroidPlayerControl(mediaController.getTransportControls()), mediaController.getMetadata(), mediaController.getPlaybackState());
        return playerHostFragment;
    }

    private TrackListFragment getTrackListFragment(TrackList trackList) {
        TrackListFragment trackListFragment = TrackListFragment.newInstance();
        if (trackList != null) {
            trackListFragment.setTitle(trackList.getDisplayName());
            trackListFragment.setTrackList(trackList);
            try {
                Track track = tracksStorageManager.getTrack(tracksStorageManager.getReference(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
                trackListFragment.setTintColor(colors.getColorResource(track.getColor()));
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
            explorerFragment.setTintColor(colors.getColorResource(track.getColor()));
        } catch (NullPointerException e) {
            //Metadata is null
            explorerFragment.setTintColor(R.color.green700);
        }
        return explorerFragment;
    }

    @Override
    public void init() {
        setContentView(R.layout.activity_tortoise);
        configureLayoutTransition();

        startService();
        registerPlayerControlReceiver();

        if (explorer == null) {
            explorer = new Explorer(this::invalidate, this);
        }

        if (tracksStorageManager == null) {
            tracksStorageManager = new TracksStorageManager(this);
        }

        if (colors == null) {
            colors = new Colors(this);
        }

        IntentFilter filter = new IntentFilter(ACTION_SHOW_TRACK_EDITOR);
        registerReceiver(showEditorReceiver, filter);
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

    private void showPlayerHostFragment() {
        replaceFragment(getPlayerHostFragment());
        hideFragment(smallPlayerFragment);
    }

    private void showTrackEditorFragment(Track.Reference reference) {
        startActivity(new Intent(this, TrackEditorActivity.class).putExtra(TrackEditorActivity.EXTRA_TRACK, reference.toJson()));
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
            showExplorerFragment();
        }
        onFragmentChanged();
    }

    private void onFragmentChanged() {
        redrawActionBar(currentFragment);
        if (settingsButton != null) {
            settingsButton.setVisible(!(currentFragment instanceof SettingsFragment));
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(showEditorReceiver);
        unregisterReceiver(showPlayerReceiver);
        explorer.unregisterReceiver(this);
        super.onDestroy();
    }

    private void showSmallPlayerFragment() {
        if (mediaController != null && mediaController.getMetadata() != null
                && (smallPlayerFragment == null || !smallPlayerFragment.isVisible())
                && mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_STOPPED
                && !(currentFragment instanceof SettingsFragment) && !(currentFragment instanceof PlayerHostFragment)) {
            SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
            smallPlayerFragment.setListener(v -> showPlayerHostFragment());
            smallPlayerFragment.setController(new AndroidPlayerControl(mediaController.getTransportControls()));
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
        if (currentFragment instanceof PlayerHostFragment) {
            currentFragment = getPlayerHostFragment();
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
