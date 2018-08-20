package ru.krivocraft.kbmp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

public class PlayerService extends Service {

    private MediaPlayer player;

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
                case Constants.ACTION_START:
                    try {
                        player = new MediaPlayer();
                        player.setDataSource(intent.getStringExtra(Constants.COMPOSITION_LOCATION));
                        player.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.ACTION_STOP:
                    player.stop();
                    break;
            }
        }
    };

    public MediaPlayer getPlayer() {
        return player;
    }
}
