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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.playback.MediaService;
import ru.krivocraft.tortoise.core.storage.SettingsStorageManager;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;
import ru.krivocraft.tortoise.fragments.EqualizerFragment;
import ru.krivocraft.tortoise.fragments.LargePlayerFragment;
import ru.krivocraft.tortoise.fragments.tracklist.TrackListFragment;

public class PlayerActivity extends BaseActivity {

    private final static int INDEX_FRAGMENT_PLAYER = 0;
    private final static int INDEX_FRAGMENT_PLAYLIST = 1;
    private ViewPager pager;

    private TrackList trackList;
    private LargePlayerFragment largePlayerFragment;
    private TrackListFragment trackListFragment;

    private boolean equalizerShown = false;
    private EqualizerFragment equalizerFragment;
    private SettingsStorageManager settingsStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        settingsStorageManager = new SettingsStorageManager(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_TRACK_LIST);
        filter.addAction(MainActivity.ACTION_HIDE_PLAYER);
        registerReceiver(receiver, filter);
    }

    @Override
    void init() {
        //Do nothing
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        //Do nothing
    }

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        //Do nothing
    }

    @Override
    void onMediaBrowserConnected() {
        sendBroadcast(new Intent(MediaService.ACTION_REQUEST_TRACK_LIST));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_equalizer) {
            changeEqualizerState();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeEqualizerState() {
        if (equalizerFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slideup, R.anim.fadeoutshort);
            if (!equalizerShown) {
                transaction.add(R.id.player_container, equalizerFragment);
            } else {
                transaction.remove(equalizerFragment);
            }
            equalizerShown = !equalizerShown;

            transaction.commitNowAllowingStateLoss();

            View view = equalizerFragment.getView();
            if (view != null) {
                if (settingsStorageManager.getOption(SettingsStorageManager.KEY_THEME, false)) {
                    view.setBackgroundResource(R.drawable.background_light);
                } else {
                    view.setBackgroundResource(R.drawable.background_dark);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (largePlayerFragment != null) {
            largePlayerFragment.requestPosition(this);
        }
    }

    private void createTrackListFragment() {
        trackListFragment = TrackListFragment.newInstance(false, PlayerActivity.this, mediaController);
        trackListFragment.setTrackList(trackList);
    }

    private void createPlayerFragment() {
        largePlayerFragment = LargePlayerFragment.newInstance(PlayerActivity.this, trackList, mediaController);
    }

    private void initPager() {
        pager = findViewById(R.id.pager_p);
        createPlayerFragment();
        createTrackListFragment();
        pager.setAdapter(new PagerAdapter());
        pager.invalidate();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainActivity.ACTION_HIDE_PLAYER.equals(intent.getAction())) {
                finish();
            } else if (MediaService.ACTION_RESULT_TRACK_LIST.equals(intent.getAction())) {
                PlayerActivity.this.trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
                equalizerFragment = EqualizerFragment.newInstance(PlayerActivity.this, TrackReference.fromJson(intent.getStringExtra(Track.EXTRA_TRACK)), mediaController);
                initPager();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        if (equalizerShown) {
            changeEqualizerState();
        } else {
            if (pager.getCurrentItem() == INDEX_FRAGMENT_PLAYLIST) {
                pager.setCurrentItem(INDEX_FRAGMENT_PLAYER);
            } else {
                super.onBackPressed();
            }
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {


        PagerAdapter() {
            super(PlayerActivity.this.getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            if (i == INDEX_FRAGMENT_PLAYER) {
                return getPlayerPage();
            } else if (i == INDEX_FRAGMENT_PLAYLIST) {
                return getTrackListPage();
            }
            return null;
        }

        @NonNull
        private LargePlayerFragment getPlayerPage() {
            return largePlayerFragment;
        }

        private TrackListFragment getTrackListPage() {
            return trackListFragment;
        }

    }
}
