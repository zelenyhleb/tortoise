package ru.krivocraft.kbmp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.tasks.ReadTrackListsTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileByAuthorTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileByTagsTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileFavoritesTask;

public class ExplorerFragment extends BaseFragment {

    private TrackListAdapter adapter;
    private OnItemClickListener listener;

    private ProgressBar progressBar;

    static ExplorerFragment newInstance(OnItemClickListener listener) {
        ExplorerFragment explorerFragment = new ExplorerFragment();
        explorerFragment.setListener(listener);
        return explorerFragment;
    }

    private void compileByAuthors() {
        Activity context = getActivity();
        if (context != null) {
            CompileByAuthorTask task = new CompileByAuthorTask();
            task.setListener(trackLists -> new Thread(() -> {
                for (Map.Entry<String, List<Track>> entry : trackLists.entrySet()) {
                    TrackList trackList = new TrackList(entry.getKey(), Tracks.getReferences(context, entry.getValue()), Constants.TRACK_LIST_BY_AUTHOR);
                    writeTrackList(trackList);
                }
                context.runOnUiThread(this::readTrackLists);
            }).start());
            task.execute(Tracks.getTrackStorage(context).toArray(new Track[0]));
        }
    }

    private void compileFavorites() {
        Activity context = getActivity();
        if (context != null) {
            CompileFavoritesTask task = new CompileFavoritesTask();
            task.setListener(trackLists -> new Thread(() -> {
                for (Map.Entry<String, List<Track>> entry : trackLists.entrySet()) {
                    TrackList trackList = new TrackList(entry.getKey(), Tracks.getReferences(context, entry.getValue()), Constants.TRACK_LIST_CUSTOM);
                    writeTrackList(trackList);
                }
                context.runOnUiThread(this::readTrackLists);
            }).start());
            task.execute(Tracks.getTrackStorage(context).toArray(new Track[0]));
        }
    }

    private void compileByTags() {
        Activity context = getActivity();
        if (context != null) {
            CompileByTagsTask task = new CompileByTagsTask();
            task.setListener(trackLists -> new Thread(() -> {
                for (Map.Entry<String, List<Track>> entry : trackLists.entrySet()) {
                    TrackList trackList = new TrackList(entry.getKey(), Tracks.getReferences(context, entry.getValue()), Constants.TRACK_LIST_BY_TAG);
                    writeTrackList(trackList);
                }
                context.runOnUiThread(this::readTrackLists);
            }).start());
            task.execute(Tracks.getTrackStorage(context).toArray(new Track[0]));
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            invalidate();
        }
    };

    private boolean getPreference(Context context, String optionKey) {
        return Utils.getOption(context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE), optionKey, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        this.progressBar = rootView.findViewById(R.id.explorer_progress);
        Context context = getContext();
        if (context != null) {
            GridView gridView = rootView.findViewById(R.id.playlists_grid);
            adapter = new TrackListAdapter(new ArrayList<>(), context);

            context.registerReceiver(receiver, new IntentFilter(Constants.Actions.ACTION_UPDATE_STORAGE));
            invalidate();

            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener((parent, view, position, id) -> listener.onItemClick((TrackList) parent.getItemAtPosition(position)));
            gridView.setOnItemLongClickListener((parent, view, position, id) -> {
                TrackList itemAtPosition = (TrackList) parent.getItemAtPosition(position);
                if (!(itemAtPosition.getDisplayName().equals(Constants.STORAGE_TRACKS_DISPLAY_NAME) || itemAtPosition.getDisplayName().equals(Constants.FAVORITES_DISPLAY_NAME))) {
                    showDeletionDialog(context, parent, position);
                }
                return true;
            });


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
                .setPositiveButton("DELETE", (dialog12, which) -> removeTrackList(item))
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

        List<Track> allTracks = Tracks.getTrackStorage(context);
        progressBar.setMax(allTracks.size());

        List<TrackReference> selectedTracks = new ArrayList<>();

        SelectableTracksAdapter adapter = new SelectableTracksAdapter(allTracks, context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Track item = (Track) parent.getItemAtPosition(position);
            TrackReference reference = new TrackReference(position);
            if (selectedTracks.contains(reference)) {
                selectedTracks.remove(reference);
                item.setCheckedInList(false);
            } else {
                selectedTracks.add(reference);
                item.setCheckedInList(true);
            }
            adapter.notifyDataSetInvalidated();
        });
        progressBar.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);

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
                if (checkTrackList(selectedTracks.size(), displayName, context)) {
                    writeTrackList(new TrackList(displayName, selectedTracks, Constants.TRACK_LIST_CUSTOM));
                    readTrackLists();
                    dialog.dismiss();
                }
            });
        });

        alertDialog.show();

    }

    private boolean checkTrackList(int arrayLength, String displayName, Context context) {
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
            SharedPreferences.Editor editor = context.getSharedPreferences(Constants.STORAGE_TRACK_LISTS, Context.MODE_PRIVATE).edit();
            editor.putString(trackList.getIdentifier(), trackList.toJson());
            editor.apply();
        }

    }

    private void removeTrackList(TrackList trackList) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(Constants.STORAGE_TRACK_LISTS, Context.MODE_PRIVATE).edit();
            editor.remove(trackList.getIdentifier());
            editor.apply();
            invalidate();
        }
    }

    private List<String> getTrackListIdentifiers() {
        Context context = getContext();
        List<String> identifiers = new ArrayList<>();
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.STORAGE_TRACK_LISTS, Context.MODE_PRIVATE);
            Map<String, ?> trackLists = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : trackLists.entrySet()) {
                identifiers.add(entry.getKey());
            }
        }
        return identifiers;
    }

    private void readTrackLists() {
        Context context = getContext();
        if (context != null) {
            SharedPreferences trackListsStorage = context.getSharedPreferences(Constants.STORAGE_TRACK_LISTS, Context.MODE_PRIVATE);
            SharedPreferences settingsStorage = context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE);

            ReadTrackListsTask task = new ReadTrackListsTask(trackListsStorage, settingsStorage, trackLists -> {
                progressBar.setVisibility(View.GONE);
                redrawList(trackLists);
            });
            task.execute();
        }
    }

    private void redrawList(List<TrackList> trackLists) {
        adapter.clear();
        adapter.addAll(trackLists);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(receiver);
        }
    }

    void invalidate() {
        Context context = getContext();
        if (context != null) {
            compileFavorites();
            if (getPreference(context, Constants.KEY_SORT_BY_ARTIST)) {
                compileByAuthors();
            }
            if (getPreference(context, Constants.KEY_SORT_BY_TAG)) {
                compileByTags();
            }
        }

        readTrackLists();
    }

    private void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(TrackList trackList);
    }

}
