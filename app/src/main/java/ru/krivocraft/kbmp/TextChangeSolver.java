package ru.krivocraft.kbmp;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextChangeSolver implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        //Nothing
    }
}
