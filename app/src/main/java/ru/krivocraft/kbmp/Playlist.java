package ru.krivocraft.kbmp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Playlist implements Serializable {

    private List<Composition> compositions = new ArrayList<>();

    public void addComposition(Composition composition) {
        compositions.add(composition);
    }

    public void removeComposition(Composition composition) {
        compositions.remove(composition);
    }

    public void shuffle() {

    }

    public void addCompositions(Collection<Composition> compositions){
        this.compositions.addAll(compositions);
    }

    public List<Composition> getCompositions() {
        return compositions;
    }

    class Adapter extends ArrayAdapter<Composition> {

        Adapter(@NonNull Context context) {
            super(context, R.layout.composition_list_item, compositions);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Composition composition = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.composition_list_item, null);
            }

            ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(composition.getName());
            ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(composition.getComposer());

            return convertView;
        }
    }
}
