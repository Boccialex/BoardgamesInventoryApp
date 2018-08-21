package com.example.franc.boardgamesinventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.content.ContentResolver;


/**
 * API Contract for the Boardgames Inventory App.
 */
public class DatabaseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DatabaseContract() {
    }

    /**
     * By adding the URI to the Contract, we represent its components by constants
     */

    // String for the Content authority
    public static final String CONTENT_AUTHORITY = "com.example.franc.boardgamesinventoryapp";

    // String for the URI (used to contact the Content Provider)
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // String for the "games" table path (which will be appended to the base content URI)
    public static final String PATH_GAMES = "games";

    /**
     * Inner class that defines constant values for the games database table.
     * Each entry in the table represents a single game.
     */
    public static final class GamesEntry implements BaseColumns {
        // The content URI to access the game data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAMES);
        // MIME type of the {@link #CONTENT_URI} for a list of games.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;
        // The MIME type of the {@link #CONTENT_URI} for a single game.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;
        // Name of database table for games
        public final static String TABLE_NAME = "games";
        //Unique ID number for the game (only for use in the database table), <p>, Type: INTEGER
        public final static String _ID = BaseColumns._ID;
        // Name of the game, Type: TEXT
        public final static String COLUMN_GAME_TITLE = "productName";
        //Gender of the pet. The only possible values are {@link #CATEGORY_PARTY_GAME}, {@link #CATEGORY_AMERICAN},
        // {@link #CATEGORY_GERMAN}, OR {@link #CATEGORY_OTHER}. Type: INTEGER
        public final static String COLUMN_GAME_CATEGORY = "category";
        //Price of the game. Type: INTEGER
        public final static String COLUMN_GAME_PRICE = "price";
        // Quantity of the game. Type: INTEGER
        public final static String COLUMN_GAME_QUANTITY = "quantity";
        // Supplier's name of the game. Type: TEXT
        public final static String COLUMN_GAME_SUPPLIER_NAME = "supplierName";
        // Quantity of the game. Type: TEXT
        public final static String COLUMN_GAME_SUPPLIER_PHONE_NUMBER = "supplierPhoneNumber";

        // Possible values for the category of the game.
        public static final int CATEGORY_OTHER = 0;
        public static final int CATEGORY_PARTY_GAME = 1;
        public static final int CATEGORY_AMERICAN = 2;
        public static final int CATEGORY_GERMAN = 3;

        // Returns whether or not the given quantity is {@link #CATEGORY_AMERICAN}, {@link #CATEGORY_GERMAN},
        // {@link #CATEGORY_PARTY_GAME}, or {@link #CATEGORY_OTHER}
        public static boolean isValidCategory(int category) {
            if (category == CATEGORY_AMERICAN || category == CATEGORY_GERMAN || category == CATEGORY_PARTY_GAME || category == CATEGORY_OTHER) {
                return true;
            }
            return false;
        }
    }
}

