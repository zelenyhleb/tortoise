package ru.krivocraft.kbmp.core;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onDragCompleted();

}
