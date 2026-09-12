package com.sn.smartpantry.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.sn.smartpantry.data.dao.PantryDao;
import com.sn.smartpantry.data.entity.PantryItem;

import java.util.List;

/**
 * Sits between the UI (ViewModels/Activities) and PantryDao, so screens never
 * call Room directly. All writes are pushed onto AppDatabase's background
 * executor, since Room forbids DB access on the main thread.
 */
public class PantryRepository {

    private final PantryDao pantryDao;
    private final LiveData<List<PantryItem>> allItems;

    public PantryRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        pantryDao = db.pantryDao();
        allItems = pantryDao.getAllItemsLive();
    }

    public LiveData<List<PantryItem>> getAllItems() {
        return allItems;
    }

    public List<PantryItem> getAllItemsSnapshot() {
        return pantryDao.getAllItemsSnapshot();
    }

    public void insert(PantryItem item) {
        AppDatabase.databaseWriteExecutor.execute(() -> pantryDao.insert(item));
    }

    public void update(PantryItem item) {
        AppDatabase.databaseWriteExecutor.execute(() -> pantryDao.update(item));
    }

    public void delete(PantryItem item) {
        AppDatabase.databaseWriteExecutor.execute(() -> pantryDao.delete(item));
    }
}
