package com.sn.smartpantry.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A single ingredient the user currently has at home.
 *
 * quantity + unit together describe how much the user has, e.g. quantity=2, unit="cup".
 * expiryDate is stored as a nullable epoch-millis Long so it can be sorted/compared easily;
 * pass null if the user didn't set one.
 */
@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "quantity")
    public double quantity;

    @ColumnInfo(name = "unit")
    public String unit;

    // Nullable: epoch millis. Null means "no expiry date set".
    @ColumnInfo(name = "expiry_date")
    public Long expiryDate;

    public PantryItem(@NonNull String name, double quantity, String unit, Long expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }
}
