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

import ru.krivocraft.kbmp.api.TrackListsCompiler;
import ru.krivocraft.kbmp.api.TrackListsStorageManager;
import ru.krivocraft.kbmp.api.TracksStorageManager;
import ru.krivocraft.kbmp.constants.Constants;

public class ExplorerFragment extends BaseFragment {

    private TrackListAdapter adapter;
    private OnItemClickListener listener;

    private TrackListsStorageManager trackListsStorageManager;
    private TracksStorageManager tracksStorageManager;
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
            activity.runOnUiThread(ExplorerFragment.this::drawTrackLists);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            invalidate();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (context != null) {
            this.trackListsStorageManager = new TrackListsStorageManager(context);
            this.tracksStorageManager = new TracksStorageManager(context);
            this.trackListsCompiler = new TrackListsCompiler(context);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        this.progressBar = rootView.findViewById(R.id.explorer_progress);
        Context context = getContext();
        if (context != null) {
            context.registerReceiver(receiver, new IntentFilter(Constants.Actions.ACTION_UPDATE_STORAGE));
            createAdapter(context);
            configureGridView(rootView, context);
            configureAddButton(inflater, rootView, context);
        }
        return rootView;
    }

    private void createAdapter(Context context) {
        if (adapter == null) {
            adapter = new TrackListAdapter(new ArrayList<>(), context);
        }
    }

    private void configureAddButton(@NonNull LayoutInflater inflater, View rootView, Context context) {
        FloatingActionButton addTrackList = rootView.findViewById(R.id.add_track_list_button);
        addTrackList.setOnClickListener(v -> showCreationDialog(inflater, context));
    }

    private void configureGridView(View rootView, Context context) {
        GridView gridView = rootView.findViewById(R.id.playlists_grid);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> listener.onItemClick((TrackList) parent.getItemAtPosition(position)));
        gridView.setOnItemLongClickListener((parent, view, position, id) -> showEditor(context, parent, position));
    }

    private boolean showEditor(Context context, AdapterView<?> parent, int position) {
        TrackList itemAtPosition = (TrackList) parent.getItemAtPosition(position);
        if (!(itemAtPosition.getDisplayName().equals(Constants.STORAGE_TRACKS_DISPLAY_NAME) || itemAtPosition.getDisplayName().equals(Constants.FAVORITES_DISPLAY_NAME))) {
            Intent intent = new Intent(context, TrackListEditorActivity.class);
            intent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, itemAtPosition.toJson());
            context.startActivity(intent);
        }
        return true;
    }

    private void showCreationDialog(@NonNull LayoutInflater inflater, Context context) {
        View view = inflater.inflate(R.layout.dialog_add_track_list, null);

        ListView listView = view.findViewById(R.id.tracks_selecting_list);
        EditText editText = view.findViewById(R.id.track_list_name);
        ProgressBar progressBar = view.findViewById(R.id.creation_dialog_progress);
        TextView textView = view.findViewById(R.id.obtaining_text);

        List<Track> allTracks = tracksStorageManager.getTrackStorage();
        progressBar.setMax(allTracks.size());

        List<TrackReference> selectedTracks = new ArrayList<>();

        SelectableTracksAdapter adapter = new SelectableTracksAdapter(allTracks, context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Track item = (Track) parent.getItemAtPosition(position);
            TrackReference reference = new TrackReference(item);
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
                    trackListsStorageManager.writeTrackList(new TrackList(displayName, selectedTracks, Constants.TRACK_LIST_CUSTOM));
                    drawTrackLists();
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
        if (trackListsStorageManager.getTrackListIdentifiers().contains(TrackList.createIdentifier(displayName))) {
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
        if (displayName.equals("empty")) {
            Toast.makeText(context, "Ha-ha, very funny. Name must not be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void drawTrackLists() {
        progressBar.setVisibility(View.GONE);
        redrawList(trackListsStorageManager.readTrackLists());
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
            trackListsCompiler.compileFavorites(this::onNewTrackLists);
            if (settingsManager.getOption(Constants.KEY_SORT_BY_ARTIST, false)) {
                trackListsCompiler.compileByAuthors(this::onNewTrackLists);
            }
            if (settingsManager.getOption(Constants.KEY_SORT_BY_TAG, false)) {
                trackListsCompiler.compileByTags(this::onNewTrackLists);
            }
        }
    }

    private void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(TrackList trackList);
    }

}
