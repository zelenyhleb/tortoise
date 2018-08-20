package ru.krivocraft.kbmp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;
import java.util.Objects;

public class PlayerService extends Service {

    private MediaPlayer player;

    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String COMPOSITION_LOCATION = "composition_location";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(receiver, new IntentFilter());
        return super.onStartCommand(intent, flags, startId);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_START:
                    try {
                        player = new MediaPlayer();
                        player.setDataSource(intent.getStringExtra(COMPOSITION_LOCATION));
                        player.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_STOP:
                    player.stop();
                    break;
            }
        }
    };

    public MediaPlayer getPlayer() {
        return player;
    }
}
