package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.krivocraft.kbmp.constants.Constants;

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
                invalidate();
            }
        }
    };

    private List<TrackList> compileByAuthors() {
        Context context = getContext();
        Map<String, TrackList> playlistMap = new HashMap<>();
        if (context != null && Utils.getOption(context, Constants.KEY_AUTO_SORT)) {
            TrackList source = readTrackList(TrackList.createIdentifier(Constants.STORAGE_DISPLAY_NAME));
            if (source != null) {
                for (String track : source.getTracks()) {
                    String artist = Utils.loadData(track, context.getContentResolver()).getArtist();
                    TrackList trackList = playlistMap.get(artist);
                    if (trackList == null) {
                        trackList = new TrackList(artist, new ArrayList<>());
                        playlistMap.put(artist, trackList);
                    }
                    if (!trackList.getTracks().contains(track)) {
                        trackList.addTrack(track);
                    }
                }
            }
        }
        return new ArrayList<>(playlistMap.values());
    }

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
            invalidate();
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener((parent, view, position, id) -> {
                listener.onItemClick((TrackList) parent.getItemAtPosition(position));
            });
            gridView.setOnItemLongClickListener((parent, view, position, id) -> {
                TrackList itemAtPosition = (TrackList) parent.getItemAtPosition(position);
                if (!itemAtPosition.getDisplayName().equals(Constants.STORAGE_DISPLAY_NAME)) {
                    showDeletionDialog(context, parent, position);
                }
                return true;
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.Actions.ACTION_UPDATE_STORAGE);
            context.registerReceiver(storageUpdateReceiver, filter);

            FloatingActionButton addTrackList = rootView.findViewById(R.id.add_track_list_button);
            addTrackList.setOnClickListener(v -> showCreationDialog(inflater, context));
        }
        return rootView;
    }

    private void showDeletionDialog(Context context, AdapterView<?> parent, int position) {
        TrackList item = (TrackList) parent.getItemAtPosition(position);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("Do you really want to delete " + item.getDisplayName() + "?")
                .setPositiveButton("DELETE", (dialog12, which) -> {
                    removeTrackList(item);
                })
                .setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss())
                .create();
        dialog.show();
    }

    private void showCreationDialog(@NonNull LayoutInflater inflater, Context context) {
        View view = inflater.inflate(R.layout.dialog_add_track_list, null);
        ListView listView = view.findViewById(R.id.tracks_selecting_list);
        EditText editText = view.findViewById(R.id.track_list_name);
        ProgressBar progressBar = view.findViewById(R.id.creation_dialog_progress);
        TextView textView = view.findViewById(R.id.obtaining_text);

        List<String> allTracks = Objects.requireNonNull(readTrackList(TrackList.createIdentifier(Constants.STORAGE_DISPLAY_NAME))).getTracks();
        progressBar.setMax(allTracks.size());

        List<String> selectedTracks = new ArrayList<>();

        LoadDataTask loadDataTask = new LoadDataTask();
        loadDataTask.setContentResolver(context.getContentResolver());
        loadDataTask.setProgressCallback(progressBar::setProgress);
        loadDataTask.setDataLoaderCallback(tracks -> {
            SelectableTracksAdapter adapter = new SelectableTracksAdapter(tracks, context);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                Track item = (Track) parent.getItemAtPosition(position);
                if (selectedTracks.contains(item.getPath())) {
                    selectedTracks.remove(item.getPath());
                    item.setChecked(false);
                } else {
                    selectedTracks.add(item.getPath());
                    item.setChecked(true);
                }
                adapter.notifyDataSetInvalidated();
            });
            progressBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
        });

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Select tracks")
                .setPositiveButton("APPLY", null)
                .setNegativeButton("CANCEL", (dialogOnCancel, which) -> dialogOnCancel.dismiss())
                .setView(view)
                .create();
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String displayName = editText.getText().toString();
                if (acceptTrackList(selectedTracks.size(), displayName, context)) {
                    writeTrackList(new TrackList(displayName, selectedTracks));
                    dialog.dismiss();
                }
            });
        });

        alertDialog.show();

        loadDataTask.execute(allTracks.toArray(new String[0]));
    }

    private boolean acceptTrackList(int arrayLength, String displayName, Context context) {
        if (displayName.length() <= 0) {
            Toast.makeText(context, "Name must not be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        if (getTrackListIdentifiers().contains(TrackList.createIdentifier(displayName))) {
            Toast.makeText(context, "The similar name already exists", Toast.LENGTH_LONG).show();
            return false;
        }
        if (displayName.length() > 20) {
            Toast.makeText(context, "Length must not exceed 20 characters", Toast.LENGTH_LONG).show();
            return false;
        }
        if (arrayLength <= 0) {
            Toast.makeText(context, "You can't create empty track list", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void writeTrackList(TrackList trackList) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(Constants.TRACK_LISTS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(trackList.getIdentifier(), trackList.toJson());
            editor.apply();
            invalidate();
        }

    }

    private void removeTrackList(TrackList trackList) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(Constants.TRACK_LISTS_NAME, Context.MODE_PRIVATE).edit();
            editor.remove(trackList.getIdentifier());
            editor.apply();
            invalidate();
        }
    }

    private TrackList readTrackList(String identifier) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACK_LISTS_NAME, Context.MODE_PRIVATE);
            return TrackList.fromJson(sharedPreferences.getString(identifier, null));
        }
        return null;
    }

    private List<String> getTrackListIdentifiers() {
        Context context = getContext();
        List<String> identifiers = new ArrayList<>();
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACK_LISTS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> trackLists = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : trackLists.entrySet()) {
                identifiers.add(entry.getKey());
            }
        }
        return identifiers;
    }

    private List<TrackList> readTrackLists() {
        Context context = getContext();
        List<TrackList> tracks = new ArrayList<>();
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACK_LISTS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> trackLists = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : trackLists.entrySet()) {
                tracks.add(TrackList.fromJson((String) entry.getValue()));
            }
        }
        return tracks;
    }

    @Override
    public void onDestroy() {
        Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(storageUpdateReceiver);
        }
        super.onDestroy();
    }

    void invalidate() {
        adapter.clear();
        adapter.addAll(readTrackLists());
        adapter.addAll(compileByAuthors());
        adapter.notifyDataSetChanged();
    }

    void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(TrackList trackList);
    }

}
