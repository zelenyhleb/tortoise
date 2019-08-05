package ru.krivocraft.kbmp;

abstract class StorageManager {

    private OnStorageUpdateCallback callback;

    StorageManager(OnStorageUpdateCallback callback){
        this.callback = callback;
    }

    void notifyListener(){
        callback.onStorageUpdate();
    }

    interface OnStorageUpdateCallback {
        void onStorageUpdate();
    }
}
