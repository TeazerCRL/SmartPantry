package com.sn.smartpantry.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sn.smartpantry.data.entity.PantryItem;

import java.util.List;

@Dao
public interface PantryDao {

    // LiveData so the Pantry List screen's RecyclerView updates automatically
    // whenever the underlying table changes (add/edit/delete).
    @Query("SELECT * FROM pantry_items ORDER BY name ASC")
    LiveData<List<PantryItem>> getAllItemsLive();

    // Plain (non-LiveData) snapshot used by the matching algorithm, which runs
    // on a background thread and needs a one-off list, not an observer.
    @Query("SELECT * FROM pantry_items")
    List<PantryItem> getAllItemsSnapshot();

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    LiveData<PantryItem> getItemById(int id);

    @Insert
    long insert(PantryItem item);

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);

    @Query("DELETE FROM pantry_items WHERE id = :id")
    void deleteById(int id);
}
