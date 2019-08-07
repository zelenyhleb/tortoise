package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExplorerFragment extends Fragment {

    private TrackListAdapter adapter;
    private OnItemClickListener listener;

    public ExplorerFragment() {
        // Required empty public constructor
    }

    public static ExplorerFragment newInstance(OnItemClickListener listener) {
        ExplorerFragment explorerFragment = new ExplorerFragment();
        explorerFragment.setListener(listener);
        return explorerFragment;
    }

    private BroadcastReceiver storageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter != null) {
                adapter.clear();
                adapter.addAll(readTrackLists());
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        Context context = getContext();
        if (context != null) {
            GridView gridView = rootView.findViewById(R.id.playlists_grid);
            adapter = new TrackListAdapter(readTrackLists(), context);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener((parent, view, position, id) -> {
                listener.onItemClick((TrackList) parent.getItemAtPosition(position));
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_UPDATE_STORAGE);
            context.registerReceiver(storageUpdateReceiver, filter);
        }
        return rootView;
    }

    private void writeTrackList(TrackList trackList) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(trackList.getIdentifier(), trackList.toJson());
            editor.apply();
        }

    }

    private TrackList readTrackList(String identifier) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
            return TrackList.fromJson(sharedPreferences.getString(identifier, null));
        }
        return null;
    }

    private List<TrackList> readTrackLists() {
        Context context = getContext();
        List<TrackList> tracks = new ArrayList<>();
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
            Map<String, ?> trackLists = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : trackLists.entrySet()) {
                tracks.add(TrackList.fromJson((String) entry.getValue()));
            }
        }
        return tracks;
    }

    void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(TrackList trackList);
    }

}
