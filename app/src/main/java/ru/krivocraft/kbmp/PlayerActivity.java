package ru.krivocraft.kbmp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

public class PlayerActivity extends AppCompatActivity {

    private MediaPlaybackService mediaPlaybackServiceInstance;
    private ViewPager pager;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlaybackServiceInstance = ((MediaPlaybackService.LocalBinder) service).getServerInstance();

            pager.setAdapter(new PagerAdapter());
            pager.invalidate();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        pager = findViewById(R.id.pager_p);
        bindService(new Intent(this, MediaPlaybackService.class), connection, BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == Constants.INDEX_FRAGMENT_PLAYLIST) {
            pager.setCurrentItem(Constants.INDEX_FRAGMENT_PLAYER);
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

        @Override
        public Fragment getItem(int i) {
            if (i == Constants.INDEX_FRAGMENT_PLAYER) {
                return getPlayerPage();
            } else if (i == Constants.INDEX_FRAGMENT_PLAYLIST) {
                return getTrackListPage();
            }
            return null;
        }

        @NonNull
        private LargePlayerFragment getPlayerPage() {
            LargePlayerFragment largePlayerFragment = new LargePlayerFragment();
            if (mediaPlaybackServiceInstance != null) {
                mediaPlaybackServiceInstance.addStateCallbackListener(largePlayerFragment);
            }
            return largePlayerFragment;
        }

        private TrackListPage getTrackListPage() {
            TrackListPage trackListPage = new TrackListPage();
            trackListPage.init(mediaPlaybackServiceInstance.getPlaylist(), new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Track track = (Track) parent.getItemAtPosition(position);
                    if (!track.equals(mediaPlaybackServiceInstance.getCurrentTrack())) {
                        mediaPlaybackServiceInstance.skipToNew(position);
                    } else {
                        if (mediaPlaybackServiceInstance.isPlaying()) {
                            mediaPlaybackServiceInstance.pause();
                        } else {
                            mediaPlaybackServiceInstance.play();
                        }
                    }
                }
            }, false, PlayerActivity.this.getApplicationContext());
            return trackListPage;
        }
    }
}
