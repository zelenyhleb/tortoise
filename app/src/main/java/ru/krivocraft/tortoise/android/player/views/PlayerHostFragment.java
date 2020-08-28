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

package ru.krivocraft.tortoise.android.player.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.android.BaseFragment;
import ru.krivocraft.tortoise.core.api.Control;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.android.player.MediaService;
import ru.krivocraft.tortoise.android.tracklist.TrackListFragment;

public class PlayerHostFragment extends BaseFragment {

    private final static int INDEX_FRAGMENT_PLAYER = 0;

    //This two fields are needed to handle ui updates.
    private LargePlayerFragment largePlayerFragment;
    private TrackListFragment trackListFragment;

    @Override
    public void changeColors(int color) {
        // Host fragment has no ui to recolor
    }

    public static PlayerHostFragment newInstance() {
        return new PlayerHostFragment();
    }

    public void setInitialData(TrackList trackList, Control controller, MediaMetadataCompat metadata, PlaybackStateCompat playbackState) {
        largePlayerFragment = LargePlayerFragment.newInstance();
        largePlayerFragment.setInitialData(metadata, playbackState, trackList);
        largePlayerFragment.setController(controller);

        trackListFragment = TrackListFragment.newInstance();
        trackListFragment.setTrackList(trackList);
        trackListFragment.setShowControls(false);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        largePlayerFragment.updateMediaMetadata(metadata);
        trackListFragment.notifyTracksStateChanged();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        largePlayerFragment.updatePlaybackState(playbackState);
        trackListFragment.notifyTracksStateChanged();
    }

    private final BroadcastReceiver trackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackList trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
            if (trackList != null) {
                trackListFragment.setTrackList(trackList);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.activity_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager pager = view.findViewById(R.id.pager_p);
        pager.setAdapter(new PlayerHostFragment.PagerAdapter());

        IntentFilter trackListFilter = new IntentFilter();
        trackListFilter.addAction(MediaService.ACTION_UPDATE_TRACK_LIST);
        trackListFilter.addAction(MediaService.ACTION_RESULT_TRACK_LIST);
        view.getContext().registerReceiver(trackListReceiver, trackListFilter);
        view.getContext().sendBroadcast(new Intent(MediaService.ACTION_REQUEST_TRACK_LIST));
    }

    @Override
    public void onDestroy() {
        Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(trackListReceiver);
        }
        super.onDestroy();
    }

    private class PagerAdapter extends FragmentPagerAdapter {


        public PagerAdapter() {
            super(PlayerHostFragment.this.getChildFragmentManager(),
                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {
            if (i == INDEX_FRAGMENT_PLAYER) {
                return largePlayerFragment;
            } else {
                return trackListFragment;
            }
        }

    }
}
