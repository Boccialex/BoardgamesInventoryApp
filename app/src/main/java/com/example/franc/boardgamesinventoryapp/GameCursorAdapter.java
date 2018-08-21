package com.example.franc.boardgamesinventoryapp;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.franc.boardgamesinventoryapp.data.DatabaseContract.GamesEntry;

/**
 * {@link GameCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of game data as its data source. This adapter knows
 * how to create list items for each row of games data in the {@link Cursor}.
 */
public class GameCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link GameCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public GameCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the game data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current game can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        /// Find individual views that we want to modify in the list item layout
        TextView titleTextView = view.findViewById(R.id.name);
        TextView quantityTextView = view.findViewById(R.id.game_quantity);
        TextView priceTextView = view.findViewById(R.id.game_price);
        Button saleButton = view.findViewById(R.id.sale_button);

        // Find the columns of the game attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_TITLE);
        int quantityColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_PRICE);

        // Read the game attributes from the Cursor for the current game
        String gameTitle = cursor.getString(titleColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        String gameQuantity = cursor.getString(quantityColumnIndex);

        // Create the strings to display in the ListView
        String gamePrice = (context.getResources().getString(R.string.currency) + " " + price + context.getResources().getString(R.string.complete_price));

        // If the game price is an empty string or null, then use a default text asking for price,
        // so the TextView isn't blank.
        if (TextUtils.isEmpty(gamePrice)) {
            gamePrice = context.getString(R.string.enter_price);
        }
        // Take the quantity String as an integer value
        final int intQuantity = Integer.valueOf(gameQuantity);
        if (intQuantity <= 1) {
            quantityTextView.setText(context.getResources().getString(R.string.on_stock) + " " + intQuantity + context.getResources().getString(R.string.item));
        } else {
            quantityTextView.setText(context.getResources().getString(R.string.on_stock) + " " + intQuantity + " " + context.getResources().getString(R.string.items));
        }

        final int boardgameId = cursor.getInt(cursor.getColumnIndex(GamesEntry._ID));

        // Update the TextViews with the attributes for the current boardgame
        titleTextView.setText(gameTitle);
        priceTextView.setText(gamePrice);

        //Reduce by 1 the product quantity when the sale button is clicked on.
        saleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (intQuantity > 0) {
                    int actualQuantity = intQuantity - 1;
                    Uri quantityUri = ContentUris.withAppendedId(GamesEntry.CONTENT_URI, boardgameId);

                    ContentValues values = new ContentValues();
                    values.put(GamesEntry.COLUMN_GAME_QUANTITY, actualQuantity);
                    context.getContentResolver().update(quantityUri, values, null, null);
                } else {
                    Toast.makeText(context, "This game is out of stock!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
