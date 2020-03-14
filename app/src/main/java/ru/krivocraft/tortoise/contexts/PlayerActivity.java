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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.playback.MediaService;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.fragments.player.LargePlayerFragment;
import ru.krivocraft.tortoise.fragments.player.PlayerController;
import ru.krivocraft.tortoise.fragments.tracklist.TrackListFragment;

public class PlayerActivity extends BaseActivity {

    private final static int INDEX_FRAGMENT_PLAYER = 0;
    private final static int INDEX_FRAGMENT_PLAYLIST = 1;
    private ViewPager pager;

    private TrackList trackList;

    //This two fields are needed to handle ui updates.
    private LargePlayerFragment largePlayerFragment;
    private TrackListFragment trackListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_TRACK_LIST);
        filter.addAction(MainActivity.ACTION_HIDE_PLAYER);
        registerReceiver(receiver, filter);
        IntentFilter trackListFilter = new IntentFilter();
        trackListFilter.addAction(MediaService.ACTION_UPDATE_TRACK_LIST);
        registerReceiver(trackListReceiver, trackListFilter);
    }

    @Override
    void init() {
        //Do nothing
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        largePlayerFragment.updatePlaybackState(newPlaybackState);
        trackListFragment.notifyTracksStateChanged();
    }

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        largePlayerFragment.updateMediaMetadata(newMetadata);
        trackListFragment.notifyTracksStateChanged();
    }

    @Override
    void onMediaBrowserConnected() {
        sendBroadcast(new Intent(MediaService.ACTION_REQUEST_TRACK_LIST));
    }

    private BroadcastReceiver trackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackList trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
            if (trackList != null) {
                trackListFragment.setTrackList(trackList);
            }
        }
    };

    private TrackListFragment createTrackListFragment() {
        trackListFragment = TrackListFragment.newInstance();
        trackListFragment.setTrackList(trackList);
        trackListFragment.setShowControls(false);
        return trackListFragment;
    }

    private LargePlayerFragment createPlayerFragment() {
        largePlayerFragment = LargePlayerFragment.newInstance();
        largePlayerFragment.setInitialData(mediaController.getMetadata(), mediaController.getPlaybackState(), trackList);
        largePlayerFragment.setController(new PlayerController() {
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
        return largePlayerFragment;
    }

    private void initPager() {
        pager = findViewById(R.id.pager_p);
        pager.setAdapter(new PagerAdapter());
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainActivity.ACTION_HIDE_PLAYER.equals(intent.getAction())) {
                finish();
            } else if (MediaService.ACTION_RESULT_TRACK_LIST.equals(intent.getAction())) {
                PlayerActivity.this.trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
                initPager();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(trackListReceiver);
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == INDEX_FRAGMENT_PLAYLIST) {
            pager.setCurrentItem(INDEX_FRAGMENT_PLAYER);
        } else {
            super.onBackPressed();
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

        @NonNull
        @Override
        public Fragment getItem(int i) {
            if (i == INDEX_FRAGMENT_PLAYER) {
                return getPlayerPage();
            } else {
                return getTrackListPage();
            }
        }

        @NonNull
        private LargePlayerFragment getPlayerPage() {
            return createPlayerFragment();
        }

        private TrackListFragment getTrackListPage() {
            return createTrackListFragment();
        }

    }
}
