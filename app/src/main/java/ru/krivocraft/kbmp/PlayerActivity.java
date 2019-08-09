package ru.krivocraft.kbmp;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private final static int INDEX_FRAGMENT_PLAYER = 0;
    private final static int INDEX_FRAGMENT_PLAYLIST = 1;
    private ViewPager pager;
    private MediaBrowserCompat mediaBrowser;
    private TrackList trackList = new TrackList("null", new ArrayList<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initMediaBrowser();
    }

    private void initMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(
                PlayerActivity.this,
                new ComponentName(PlayerActivity.this, MediaPlaybackService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                            MediaControllerCompat controller = new MediaControllerCompat(PlayerActivity.this, token);
                            MediaControllerCompat.setMediaController(PlayerActivity.this, controller);
                            initPager();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.e("TAG", "onConnectionFailed");
                        Toast.makeText(PlayerActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionSuspended() {
                        Log.e("TAG", "onConnectionSuspended");
                    }
                },
                null);
        mediaBrowser.connect();
    }

    private void initPager() {
        pager = findViewById(R.id.pager_p);
        pager.setAdapter(new PagerAdapter());
        pager.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            return LargePlayerFragment.newInstance(PlayerActivity.this);
        }

        private TrackListFragment getTrackListPage() {
            return TrackListFragment.newInstance(trackList, false, PlayerActivity.this);
        }
    }
}
