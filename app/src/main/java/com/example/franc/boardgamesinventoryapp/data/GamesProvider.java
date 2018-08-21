package com.example.franc.boardgamesinventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.franc.boardgamesinventoryapp.data.DatabaseContract.GamesEntry;

/**
 * {@link ContentProvider} for Games app.
 */

public class GamesProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = GamesProvider.class.getSimpleName();

    // URI matcher code for the content URI for the games table
    private static final int GAMES = 100;

    // URI matcher code for the content URI for a single game in the games table
    private static final int GAME_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code
    // (the input passed into the constructor represents the code to return for the root URI)
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // URI pattern for the games table
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_GAMES, GAMES);

        // URI pattern for a single row in within the games table
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_GAMES + "/#", GAME_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                // For the GAMES code, query the games table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(DatabaseContract.GamesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case GAME_ID:
                // For the GAME_ID code, extract out the ID from the URI.
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = DatabaseContract.GamesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the games table to return a Cursor containing that row of the table.
                cursor = database.query(DatabaseContract.GamesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor: if a data at this URI changes, we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return insertGame(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertGame(Uri uri, ContentValues values) {

        // Check that the title is not null
        String title = values.getAsString(DatabaseContract.GamesEntry.COLUMN_GAME_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Game requires a title");
        }

        // Check that the category is not null
        Integer category = values.getAsInteger(DatabaseContract.GamesEntry.COLUMN_GAME_CATEGORY);
        if (category == null || !DatabaseContract.GamesEntry.isValidCategory(category)) {
            throw new IllegalArgumentException("Game requires a category");
        }

        // Check that the price is not null
        Integer price = values.getAsInteger(DatabaseContract.GamesEntry.COLUMN_GAME_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Game requires valid price");
        }

        // Check that the quantity is not null
        Integer quantity = values.getAsInteger(DatabaseContract.GamesEntry.COLUMN_GAME_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Game requires a quantity");
        }

        // Check that the supplier's name is not null
        String supplierName = values.getAsString(DatabaseContract.GamesEntry.COLUMN_GAME_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Game requires a supplier's name");
        }

        // Check that the supplier's phone number is not null
        String supplierPhoneNumber = values.getAsString(DatabaseContract.GamesEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER);
        if (supplierPhoneNumber == null) {
            throw new IllegalArgumentException("Game requires a supplier's phone number");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new game with the given values
        long id = database.insert(DatabaseContract.GamesEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the game content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return updateGame(uri, contentValues, selection, selectionArgs);
            case GAME_ID:
                // For the GAME_ID code, extract out the ID from the URI, so we know which row to update.
                selection = DatabaseContract.GamesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateGame(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    //Update games in the database with the given content values, applying the changes to the rows
    // specified and returning the number of rows that were successfully updated.
    private int updateGame(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link GamesEntry#COLUMN_GAME_TITLE} key is present, check that the name value is not null.
        if (values.containsKey(DatabaseContract.GamesEntry.COLUMN_GAME_TITLE)) {
            String title = values.getAsString(DatabaseContract.GamesEntry.COLUMN_GAME_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Boardgame requires a title");
            }
        }

        // If the {@link GamesEntry#COLUMN_GAME_CATEGORY} key is present, check that the gender value is valid.
        if (values.containsKey(DatabaseContract.GamesEntry.COLUMN_GAME_CATEGORY)) {
            Integer category = values.getAsInteger(DatabaseContract.GamesEntry.COLUMN_GAME_CATEGORY);
            if (category == null || !DatabaseContract.GamesEntry.isValidCategory(category)) {
                throw new IllegalArgumentException("Boardgame requires valid category");
            }
        }

        // If the {@link GamesEntry#COLUMN_GAME_PRICE} key is present, check that the price value is valid.
        if (values.containsKey(DatabaseContract.GamesEntry.COLUMN_GAME_PRICE)) {
            Integer price = values.getAsInteger(DatabaseContract.GamesEntry.COLUMN_GAME_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Boardgame requires valid price");
            }
        }

        // If the {@link GamesEntry#COLUMN_GAME_QUANTITY} key is present, check that the quantity value is valid.
        if (values.containsKey(DatabaseContract.GamesEntry.COLUMN_GAME_QUANTITY)) {
            Integer quantity = values.getAsInteger(DatabaseContract.GamesEntry.COLUMN_GAME_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Boardgame requires valid quantity");
            }
        }

        // If the {@link GamesEntry#COLUMN_GAME_SUPPLIER_NAME} key is present, check that the supplier's name value is valid.
        if (values.containsKey(DatabaseContract.GamesEntry.COLUMN_GAME_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(DatabaseContract.GamesEntry.COLUMN_GAME_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Game requires valid supplier's name");
            }
        }

        // If the {@link GamesEntry#COLUMN_GAME_SUPPLIER_PHONE_NUMBER} key is present, check that the supplier's phone number value is valid.
        if (values.containsKey(DatabaseContract.GamesEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER)) {
            String supplierPhoneNumber = values.getAsString(DatabaseContract.GamesEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER);
            if (supplierPhoneNumber == null) {
                throw new IllegalArgumentException("Game requires valid supplier's phone number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(DatabaseContract.GamesEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                rowsDeleted = database.delete(DatabaseContract.GamesEntry.TABLE_NAME, selection, selectionArgs);
            case GAME_ID:
                // Delete a single row given by the ID in the URI
                selection = DatabaseContract.GamesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(DatabaseContract.GamesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return DatabaseContract.GamesEntry.CONTENT_LIST_TYPE;
            case GAME_ID:
                return DatabaseContract.GamesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}


