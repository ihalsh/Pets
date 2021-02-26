package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.pets.data.PetsContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shelter.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA = ", ";

    //CREATE TABLE pets (_id INTEGER, name TEXT, breed TEXT, gender INTEGER, weight INTEGER);
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
                    PetEntry._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA +
                    PetEntry.COLUMN_PET_NAME + TEXT_TYPE + " NOT NULL" + COMMA +
                    PetEntry.COLUMN_PET_BREED + TEXT_TYPE + COMMA +
                    PetEntry.COLUMN_PET_GENDER + INTEGER_TYPE + " NOT NULL" + COMMA +
                    PetEntry.COLUMN_PET_WEIGHT + INTEGER_TYPE + " NOT NULL DEFAULT 0" + ")";

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
