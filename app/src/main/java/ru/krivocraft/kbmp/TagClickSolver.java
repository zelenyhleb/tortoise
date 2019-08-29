package ru.krivocraft.kbmp;

import co.lujun.androidtagview.TagView;

public abstract class TagClickSolver implements TagView.OnTagClickListener {

    @Override
    public void onTagClick(int position, String text) {
        //Nothing
    }

    @Override
    public void onSelectedTagDrag(int position, String text) {
        //Nothing
    }

    @Override
    public void onTagCrossClick(int position) {
        //Nothing
    }
}
