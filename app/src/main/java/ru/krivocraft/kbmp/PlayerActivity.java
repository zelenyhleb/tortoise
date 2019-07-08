package ru.krivocraft.kbmp;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private ViewPager pager;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaControllerCompat;

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
                            PlayerActivity.this.mediaControllerCompat = controller;
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
            largePlayerFragment.initControls(PlayerActivity.this);
            return largePlayerFragment;
        }

        private TrackListPage getTrackListPage() {
            final TrackListPage trackListPage = new TrackListPage();
            AdapterView.OnItemClickListener onListItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    String path = (String) adapterView.getItemAtPosition(position);
                    ArrayList<String> trackList = trackListPage.getTrackList();
                    MediaMetadataCompat metadata = mediaControllerCompat.getMetadata();
                    if (metadata == null) {
                        mediaControllerCompat.getTransportControls().skipToQueueItem(trackList.indexOf(path));
                    } else {
                        if (!metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).equals(path)) {
                            mediaControllerCompat.getTransportControls().skipToQueueItem(trackList.indexOf(path));
                        } else {
                            if (mediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                                mediaControllerCompat.getTransportControls().pause();
                            } else {
                                mediaControllerCompat.getTransportControls().play();
                            }
                        }
                    }
                }
            };
            trackListPage.init(onListItemClickListener, false, PlayerActivity.this);
            return trackListPage;
        }
    }
}
