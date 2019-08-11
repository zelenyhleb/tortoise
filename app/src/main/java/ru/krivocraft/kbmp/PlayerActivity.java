package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import ru.krivocraft.kbmp.constants.Constants;

public class PlayerActivity extends AppCompatActivity {

    private final static int INDEX_FRAGMENT_PLAYER = 0;
    private final static int INDEX_FRAGMENT_PLAYLIST = 1;
    private ViewPager pager;
    private MediaBrowserCompat mediaBrowser;

    private TrackList trackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initMediaBrowser();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Actions.ACTION_RESULT_TRACK_LIST);
        registerReceiver(receiver, filter);
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
                            sendBroadcast(new Intent(Constants.Actions.ACTION_REQUEST_TRACK_LIST));
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PlayerActivity.this.trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
            initPager();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
        unregisterReceiver(receiver);
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
            return LargePlayerFragment.newInstance(PlayerActivity.this, trackList);
        }

        private TrackListFragment getTrackListPage() {
            return TrackListFragment.newInstance(trackList, false);
        }
    }
}
