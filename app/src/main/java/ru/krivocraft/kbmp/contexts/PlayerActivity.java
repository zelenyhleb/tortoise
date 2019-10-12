package ru.krivocraft.kbmp.contexts;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.playback.MediaService;
import ru.krivocraft.kbmp.core.track.Track;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.core.track.TrackReference;
import ru.krivocraft.kbmp.fragments.EqualizerFragment;
import ru.krivocraft.kbmp.fragments.LargePlayerFragment;
import ru.krivocraft.kbmp.fragments.TrackListFragment;

public class PlayerActivity extends BaseActivity {

    private final static int INDEX_FRAGMENT_PLAYER = 0;
    private final static int INDEX_FRAGMENT_PLAYLIST = 1;
    private ViewPager pager;
    private MediaBrowserCompat mediaBrowser;

    private TrackList trackList;
    private LargePlayerFragment largePlayerFragment;
    private TrackListFragment trackListFragment;

    private boolean equalizerShown = false;
    private EqualizerFragment equalizerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initMediaBrowser();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_RESULT_TRACK_LIST);
        filter.addAction(MainActivity.ACTION_HIDE_PLAYER);
        registerReceiver(receiver, filter);
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
        }
    }

    private void initMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(
                PlayerActivity.this,
                new ComponentName(PlayerActivity.this, AndroidMediaService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                            MediaControllerCompat controller = new MediaControllerCompat(PlayerActivity.this, token);
                            MediaControllerCompat.setMediaController(PlayerActivity.this, controller);
                            sendBroadcast(new Intent(MediaService.ACTION_REQUEST_TRACK_LIST));
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

    @Override
    protected void onResume() {
        super.onResume();
        if (largePlayerFragment != null) {
            largePlayerFragment.requestPosition(this);
        }
    }

    private void createTrackListFragment() {
        trackListFragment = TrackListFragment.newInstance(trackList, false, PlayerActivity.this);
    }

    private void createPlayerFragment() {
        largePlayerFragment = LargePlayerFragment.newInstance(PlayerActivity.this, trackList);
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
                equalizerFragment = EqualizerFragment.newInstance(PlayerActivity.this, TrackReference.fromJson(intent.getStringExtra(Track.EXTRA_TRACK)));
                initPager();
            }
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
