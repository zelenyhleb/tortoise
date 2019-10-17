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

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.OldStuffCollector;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.fragments.ExplorerFragment;
import ru.krivocraft.kbmp.fragments.SmallPlayerFragment;
import ru.krivocraft.kbmp.fragments.TrackListFragment;

public class MainActivity extends BaseActivity {

    private SmallPlayerFragment smallPlayerFragment;

    private int viewState = 0;
    private static final int STATE_EXPLORER = 1;
    private static final int STATE_TRACK_LIST = 2;

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
    private TrackListFragment trackListFragment;
    private ExplorerFragment explorerFragment;

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        showSmallPlayerFragment();
    }

    @Override
    void onMediaBrowserConnected() {
        showSmallPlayerFragment();
        showExplorerFragment();
    }

    @NonNull
    private TrackListFragment getTrackListFragment(TrackList trackList) {
        trackListFragment = TrackListFragment.newInstance(trackList, true, this, mediaController);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExplorerFragment() {
        removeTrackListFragment();
        if (explorerFragment == null) {
            addFragment(R.anim.fadeinshort, getExplorerFragment(), R.id.fragment_container);
        } else {
            showFragment(explorerFragment);
        }
        viewState = STATE_EXPLORER;
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Tortoise");
        }
    }

    private void showTrackListFragment(TrackList trackList) {
        hideExplorerFragment();
        addFragment(R.anim.fadeinshort, getTrackListFragment(trackList), R.id.fragment_container);
        viewState = STATE_TRACK_LIST;
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(trackList.getDisplayName());
        }
    }

    @Override
    public void onBackPressed() {
        if (viewState == STATE_TRACK_LIST) {
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
        if (smallPlayerFragment != null) {
            smallPlayerFragment.requestPosition(this);
        }
    }

    private void hideExplorerFragment() {
        hideFragment(explorerFragment);
    }

    private void removeTrackListFragment() {
        removeFragment(trackListFragment);
        trackListFragment = null;
    }

    private void showSmallPlayerFragment() {
        if (mediaController.getMetadata() != null) {
            if (smallPlayerFragment == null) {
                SmallPlayerFragment smallPlayerFragment = new SmallPlayerFragment();
                smallPlayerFragment.init(MainActivity.this, mediaController);
                MainActivity.this.smallPlayerFragment = smallPlayerFragment;
                addFragment(R.anim.slideup, MainActivity.this.smallPlayerFragment, R.id.player_container);
            } else {
                this.smallPlayerFragment.invalidate();
            }
        }
    }

    private void hideSmallPlayerFragment() {
        hideFragment(smallPlayerFragment);
        smallPlayerFragment = null;
    }


    private void addFragment(int animationIn, Fragment fragment, int container) {
        if (fragment != null && !fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animationIn, R.anim.slide_out_right)
                    .add(container, fragment)
                    .commitNowAllowingStateLoss();
        }
    }

    private void showFragment(Fragment fragment) {
        if (fragment != null && !fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(fragment)
                    .commitNowAllowingStateLoss();
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

    private void removeFragment(Fragment fragment) {
        if (fragment != null && fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitNowAllowingStateLoss();
        }
    }

}
