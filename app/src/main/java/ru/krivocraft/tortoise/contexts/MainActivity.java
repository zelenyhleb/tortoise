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
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.OldStuffCollector;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;
import ru.krivocraft.tortoise.fragments.BaseFragment;
import ru.krivocraft.tortoise.fragments.SettingsFragment;
import ru.krivocraft.tortoise.fragments.SmallPlayerFragment;
import ru.krivocraft.tortoise.fragments.TrackEditorFragment;
import ru.krivocraft.tortoise.fragments.explorer.Explorer;
import ru.krivocraft.tortoise.fragments.explorer.ExplorerFragment;
import ru.krivocraft.tortoise.fragments.tracklist.TrackListFragment;

public class MainActivity extends BaseActivity {


    private SmallPlayerFragment smallPlayerFragment;

    private int viewState = 0;
    private static final int STATE_EXPLORER = 1;
    private static final int STATE_TRACK_LIST = 2;
    private static final int STATE_SETTINGS = 3;
    private static final int STATE_TRACK_EDITOR = 4;

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

    private TrackList cache;

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        showSmallPlayerFragment();
    }

    @Override
    void onMediaBrowserConnected() {
        showSmallPlayerFragment();
    }

    private TrackListFragment getTrackListFragment(TrackList trackList) {
        TrackListFragment trackListFragment = TrackListFragment.newInstance(true, this, mediaController);
        if (trackList != null)
            trackListFragment.setTrackList(trackList);
        return trackListFragment;
    }

    private ExplorerFragment getExplorerFragment() {
        return ExplorerFragment.newInstance(this::showTrackListFragment, explorer);
    }

    private TrackEditorFragment getTrackEditorFragment(TrackReference reference) {
        return TrackEditorFragment.newInstance(this::showExplorerFragment, reference);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        explorer = new Explorer(this::invalidate, this);

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

        IntentFilter filter = new IntentFilter(ACTION_SHOW_TRACK_EDITOR);
        registerReceiver(showEditorReceiver, filter);
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        showSmallPlayerFragment();
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
        runOnUiThread(currentFragment::invalidate);
    }

    private void restoreState() {
        if (viewState == STATE_TRACK_LIST) {
            if (cache != null) {
                showTrackListFragment(cache);
                return;
            }
        }
        showExplorerFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
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
        replaceFragment(getExplorerFragment(), "Tortoise", STATE_EXPLORER);
        showSmallPlayerFragment();
    }

    private void showTrackListFragment(TrackList trackList) {
        replaceFragment(getTrackListFragment(trackList), trackList.getDisplayName(), STATE_TRACK_LIST);
        this.cache = trackList;
        showSmallPlayerFragment();
    }

    private void showSettingsFragment() {
        replaceFragment(SettingsFragment.newInstance(), "Settings", STATE_SETTINGS);
        hideFragment(smallPlayerFragment);
    }

    private void showTrackEditorFragment(TrackReference trackReference) {
        replaceFragment(getTrackEditorFragment(trackReference), "Edit Metadata", STATE_TRACK_EDITOR);
        hideFragment(smallPlayerFragment);
    }

    private void replaceFragment(BaseFragment fragment, String title, int boundState) {
        replaceFragment(fragment);
        viewState = boundState;
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewState == STATE_TRACK_EDITOR) {
            currentFragment.onBackPressed();
        } else if (viewState != STATE_EXPLORER) {
            showExplorerFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(showPlayerReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreState();
        showSmallPlayerFragment();
        if (smallPlayerFragment != null) {
            smallPlayerFragment.requestPosition(this);
        }
    }

    private void showSmallPlayerFragment() {
        if (mediaController != null)
            if (mediaController.getMetadata() != null) {
                if (smallPlayerFragment == null || !smallPlayerFragment.isVisible()) {
                    SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
                    smallPlayerFragment.init(MainActivity.this, mediaController);
                    MainActivity.this.smallPlayerFragment = smallPlayerFragment;
                    showFragment(smallPlayerFragment);
                }
            }
    }


    private void replaceFragment(BaseFragment fragment) {
        if (fragment != null) {

            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fadeinshort, R.anim.fadeoutshort);

            transaction.replace(R.id.fragment_container, fragment);

            transaction.commitNowAllowingStateLoss();

            currentFragment = fragment;
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
