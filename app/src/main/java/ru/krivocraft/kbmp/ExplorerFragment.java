package ru.krivocraft.kbmp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import ru.krivocraft.kbmp.api.OnTrackListsCompiledCallback;
import ru.krivocraft.kbmp.api.TrackListsCompiler;
import ru.krivocraft.kbmp.api.TrackListsStorageManager;
import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.tasks.compilers.CompileByAuthorTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileByTagsTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileFavoritesTask;

public class ExplorerFragment extends BaseFragment {

    private TrackListAdapter adapter;
    private OnItemClickListener listener;

    private TrackListsStorageManager trackListsStorageManager;
    private TrackListsCompiler trackListsCompiler;

    private ProgressBar progressBar;

    static ExplorerFragment newInstance(OnItemClickListener listener) {
        ExplorerFragment explorerFragment = new ExplorerFragment();
        explorerFragment.setListener(listener);
        return explorerFragment;
    }

    private void onNewTrackLists(List<TrackList> trackLists) {
        Activity activity = getActivity();
        if (activity != null) {
            trackListsStorageManager.writeTrackLists(trackLists);
            activity.runOnUiThread(ExplorerFragment.this::readTrackLists);
        }
    }

    private void compileByAuthors() {
        trackListsCompiler.compileByAuthors(this::onNewTrackLists);
    }

    private void compileFavorites() {
        trackListsCompiler.compileFavorites(this::onNewTrackLists);
    }

    private void compileByTags() {
        trackListsCompiler.compileByTags(this::onNewTrackLists);
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
        Context context = getContext();
        if (context != null) {
            this.trackListsStorageManager = new TrackListsStorageManager(context);
            this.trackListsCompiler = new TrackListsCompiler(context);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        this.progressBar = rootView.findViewById(R.id.explorer_progress);
        Context context = getContext();
        if (context != null) {
            GridView gridView = rootView.findViewById(R.id.playlists_grid);
            if (adapter == null) {
                adapter = new TrackListAdapter(new ArrayList<>(), context);
            }
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
        trackListsStorageManager.writeTrackList(trackList);
    }

    private void removeTrackList(TrackList trackList) {
        trackListsStorageManager.removeTrackList(trackList);
    }

    private List<String> getTrackListIdentifiers() {
        return trackListsStorageManager.getTrackListIdentifiers();
    }

    private void readTrackLists() {
        trackListsStorageManager.readTrackLists(trackLists -> {
            progressBar.setVisibility(View.GONE);
            redrawList(trackLists);
        });
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
