package com.example.franc.boardgamesinventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.franc.boardgamesinventoryapp.data.DatabaseContract.GamesEntry;

/**
 * Database helper for the Boardgame Inventory App
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Name and version of the database file
     */
    private static final String DATABASE_NAME = "boardgames.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link DatabaseHelper}.
     *
     * @param context of the app
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_GAMES_TABLE = "CREATE TABLE " + GamesEntry.TABLE_NAME + " ("
                + GamesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GamesEntry.COLUMN_GAME_TITLE + " TEXT NOT NULL, "
                + GamesEntry.COLUMN_GAME_CATEGORY + " INTEGER NOT NULL, "
                + GamesEntry.COLUMN_GAME_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + GamesEntry.COLUMN_GAME_QUANTITY + " INTEGER NOT NULL, "
                + GamesEntry.COLUMN_GAME_SUPPLIER_NAME + " TEXT NOT NULL, "
                + GamesEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_GAMES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException("Can't downgrade database from version " +
                oldVersion + " to " + newVersion);
    }
}