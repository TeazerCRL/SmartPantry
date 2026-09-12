package com.sn.smartpantry.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sn.smartpantry.data.dao.PantryDao;
import com.sn.smartpantry.data.dao.RecipeDao;
import com.sn.smartpantry.data.entity.PantryItem;
import com.sn.smartpantry.data.entity.Recipe;
import com.sn.smartpantry.data.entity.RecipeIngredient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();

    private static volatile AppDatabase INSTANCE;

    // Shared background executor for one-off DB work (seeding, and any DAO
    // calls made outside of Room's own LiveData/Flow machinery).
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "smart_pantry_db")
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Runs exactly once, the first time the database file is created
    // (i.e. first app run after install). This is where the 15-20 seed
    // recipes get loaded in, per the assignment brief.
    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                RecipeDao recipeDao = INSTANCE.recipeDao();
                RecipeSeeder.seed(recipeDao);
            });
        }
    };
}
