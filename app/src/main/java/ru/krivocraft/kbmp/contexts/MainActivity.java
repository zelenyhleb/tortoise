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

package ru.krivocraft.kbmp.contexts;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.OldStuffCollector;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.fragments.BaseFragment;
import ru.krivocraft.kbmp.fragments.ExplorerFragment;
import ru.krivocraft.kbmp.fragments.SettingsFragment;
import ru.krivocraft.kbmp.fragments.SmallPlayerFragment;
import ru.krivocraft.kbmp.fragments.TrackListFragment;

public class MainActivity extends BaseActivity {

    private SmallPlayerFragment smallPlayerFragment;

    private int viewState = 0;
    private static final int STATE_EXPLORER = 1;
    private static final int STATE_TRACK_LIST = 2;
    private static final int STATE_SETTINGS = 3;

    public static final String ACTION_HIDE_PLAYER = "action_hide_player";
    public static final String ACTION_SHOW_PLAYER = "action_show_player";

    private BroadcastReceiver showPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SHOW_PLAYER.equals(intent.getAction())) {
                showSmallPlayerFragment();
            } else if (ACTION_HIDE_PLAYER.equals(intent.getAction())) {
                hideSmallPlayerFragment();
            }
        }
    };

    private ExplorerFragment explorerFragment;
    private TrackListFragment trackListFragment;
    private BaseFragment currentFragment;

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        showSmallPlayerFragment();
    }

    @Override
    void onMediaBrowserConnected() {
        showSmallPlayerFragment();
    }

    @NonNull
    private TrackListFragment getTrackListFragment(TrackList trackList) {
        if (trackListFragment == null) {
            trackListFragment = TrackListFragment.newInstance(true, this, mediaController);
        }
        trackListFragment.setTrackList(trackList);
        return trackListFragment;
    }

    private ExplorerFragment getExplorerFragment() {
        if (explorerFragment == null) {
            //ExplorerFragment is singleton, so we will reuse it, if it is possible
            explorerFragment = ExplorerFragment.newInstance(this::showTrackListFragment);
        }
        return explorerFragment;
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
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        //No update for new state in MainActivity
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
        showFragment(getExplorerFragment(), "Tortoise", STATE_EXPLORER, R.anim.fadeinshort);
    }

    private void showTrackListFragment(TrackList trackList) {
        showFragment(getTrackListFragment(trackList), trackList.getDisplayName(), STATE_TRACK_LIST, R.anim.fadeinshort);
    }

    private void showSettingsFragment() {
        showFragment(SettingsFragment.newInstance(), "Settings", STATE_SETTINGS, R.anim.fadeinshort);
    }

    private void showFragment(BaseFragment fragment, String title, int boundState, int animation) {
        showFragment(fragment, R.id.fragment_container, animation);
        viewState = boundState;
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewState != STATE_EXPLORER) {
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
    protected void onResume() {
        super.onResume();
        showExplorerFragment();
        if (smallPlayerFragment != null) {
            smallPlayerFragment.requestPosition(this);
        }
    }

    private void showSmallPlayerFragment() {
        if (mediaController.getMetadata() != null) {
            if (smallPlayerFragment == null) {
                SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
                smallPlayerFragment.init(MainActivity.this, mediaController);
                MainActivity.this.smallPlayerFragment = smallPlayerFragment;
                showFragment(MainActivity.this.smallPlayerFragment, R.id.player_container, R.anim.slideup);
            } else {
                this.smallPlayerFragment.invalidate();
            }
        }
    }

    private void hideSmallPlayerFragment() {
        hideFragment(smallPlayerFragment);
        smallPlayerFragment = null;
    }

    private void showFragment(BaseFragment fragment, int container, int animation) {
        if (fragment != null && !fragment.isVisible()) {

            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animation, R.anim.fadeoutshort);

            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            if (!fragment.isAdded()) {
                transaction.add(container, fragment);
            } else {
                transaction.show(fragment);
            }

            transaction.commitNowAllowingStateLoss();

            currentFragment = fragment;
        }
    }

    private void hideFragment(Fragment fragment) {
        if (fragment != null && fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(fragment)
                    .commitNowAllowingStateLoss();
        }
    }

}
