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

package ru.krivocraft.kbmp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.contexts.MainActivity;
import ru.krivocraft.kbmp.core.playback.MediaService;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;
import ru.krivocraft.kbmp.core.track.TrackList;

public class PlayerRootFragment extends BaseFragment {

    private final static int INDEX_FRAGMENT_PLAYER = 0;
    private final static int INDEX_FRAGMENT_PLAYLIST = 1;
    private ViewPager pager;
    private View rootView;

    private TrackList trackList;
    private LargePlayerFragment largePlayerFragment;
    private TrackListFragment trackListFragment;
    private MediaControllerCompat mediaController;

    private boolean equalizerShown = false;
    private EqualizerFragment equalizerFragment;

    public static PlayerRootFragment newInstance(MediaControllerCompat mediaController) {
        PlayerRootFragment playerRootFragment = new PlayerRootFragment();
        playerRootFragment.setMediaController(mediaController);
        return playerRootFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_player, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_TRACK_LIST);
        filter.addAction(MainActivity.ACTION_HIDE_PLAYER);
        view.getContext().registerReceiver(receiver, filter);

        view.getContext().sendBroadcast(new Intent(MediaService.ACTION_REQUEST_TRACK_LIST));
    }

    private void changeEqualizerState() {
        if (equalizerFragment != null) {
            FragmentTransaction transaction = getChildFragmentManager()
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
                if (getSettingsManager().getOption(SettingsStorageManager.KEY_THEME, false)) {
                    view.setBackgroundResource(R.drawable.background_light);
                } else {
                    view.setBackgroundResource(R.drawable.background_dark);
                }
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainActivity.ACTION_HIDE_PLAYER.equals(intent.getAction())) {
                requireActivity().onBackPressed();
            } else if (MediaService.ACTION_RESULT_TRACK_LIST.equals(intent.getAction())) {
                trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
//                equalizerFragment = EqualizerFragment.newInstance(requireActivity(), TrackReference.fromJson(intent.getStringExtra(Track.EXTRA_TRACK)), mediaController);
                initPager();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(receiver);
    }

    private void createTrackListFragment() {
        trackListFragment = TrackListFragment.newInstance(false, requireActivity(), mediaController);
        trackListFragment.setTrackList(trackList);
    }

    private void createPlayerFragment() {
        largePlayerFragment = LargePlayerFragment.newInstance(requireActivity(), trackList, mediaController);
    }

    private void initPager() {
        pager = rootView.findViewById(R.id.pager_p);
        createPlayerFragment();
        createTrackListFragment();
        pager.setAdapter(new PagerAdapter(getFragmentManager()));
        pager.invalidate();
    }

    @Override
    public void invalidate() {
        if (largePlayerFragment != null) {
            largePlayerFragment.requestPosition(requireContext());
        }
    }

    public void setMediaController(MediaControllerCompat mediaController) {
        this.mediaController = mediaController;
    }

    private class PagerAdapter extends FragmentPagerAdapter {


        PagerAdapter(FragmentManager fm) {
            super(fm);
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
