package ru.krivocraft.kbmp;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onDragCompleted();

}
