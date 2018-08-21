package com.example.franc.boardgamesinventoryapp;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.franc.boardgamesinventoryapp.data.DatabaseContract.GamesEntry;

/**
 * Allows user to create a new game or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the game data loader
    private static final int EXISTING_GAME_LOADER = 0;

    // Content URI for the existing game (null if it's a new game)
    private Uri mCurrentGameUri;

    // Int quantity for quantity check purposes
    private int modifyQuantity;

    // EditText fields to enter games' Title, Price, Category, Quantity Supplier's Name, Supplier's Phone Number
    private EditText mTitleEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierContactsEditText;
    private Spinner mCategorySpinner;

    // Category for a game. The possible values are: 0 for other, 1 for party game, 2 for american, 3 for german.
    private int mCategory = GamesEntry.CATEGORY_OTHER;

    // Boolean flag that keeps track of whether the game has been edited (true) or not (false)
    private boolean mGameHasChanged = false;

    // Listens for any user touches on a View (implying a modification)
    // and change the mGameHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGameHasChanged = true;
            return false;
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new game or editing an existing one.
        Intent intent = getIntent();
        mCurrentGameUri = intent.getData();

        ImageButton mSupplierContactsButton = findViewById(R.id.callButton);

        // If the intent DOES NOT contain a game content URI, then we know that we are
        // creating a new game.
        if (mCurrentGameUri == null) {
            // This is a new game, so change the app bar to say "Add a Game"
            setTitle(getString(R.string.editor_activity_title_new_game));

            // Hide the "Delete" menu option and the callButton (useless in this case)
            invalidateOptionsMenu();
            mSupplierContactsButton.setVisibility(View.GONE);
        } else {
            // Otherwise this is an existing game, so change app bar to say "Edit Game"
            setTitle(getString(R.string.editor_activity_title_edit_game));

            // Initialize a loader to read the game data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_GAME_LOADER, null, this);

            // Handles the callButton (useful in this case)
            mSupplierContactsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + mSupplierContactsEditText.getText()));
                    startActivity(callIntent);

                }
            });
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = findViewById(R.id.edit_game_name);
        mPriceEditText = findViewById(R.id.edit_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierContactsEditText = findViewById(R.id.edit_supplier_phone_number);
        mCategorySpinner = findViewById(R.id.spinner_category);

        // Buttons for increasing and decreasing quantity
        ImageButton mIncreaseQuantity = findViewById(R.id.arrow_increase);
        ImageButton mDecreaseQuantity = findViewById(R.id.arrow_decrease);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mTitleEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierContactsEditText.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        mIncreaseQuantity.setOnTouchListener(mTouchListener);
        mDecreaseQuantity.setOnTouchListener(mTouchListener);

        // When the increase button is clicked on the quantity displayed increases by 1.
        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String actualQuantity = mQuantityEditText.getText().toString();
                if (TextUtils.isEmpty(actualQuantity)) {
                    // Sanity check for empty quantity field
                    Toast.makeText(EditorActivity.this, R.string.empty_forbidden, Toast.LENGTH_SHORT).show();
                } else {
                    modifyQuantity = Integer.parseInt(actualQuantity);
                    mQuantityEditText.setText(String.valueOf(modifyQuantity + 1));
                }
            }
        });

        // When the decrease button is clicked on the quantity displayed decreases by 1.
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String actualQuantity = mQuantityEditText.getText().toString();
                // Sanity check for empty quantity field
                if (TextUtils.isEmpty(actualQuantity)) {
                    Toast.makeText(EditorActivity.this, R.string.empty_forbidden, Toast.LENGTH_SHORT).show();
                } else {
                    modifyQuantity = Integer.parseInt(actualQuantity);
                    // Sanity check for quantity lower than one
                    if ((modifyQuantity - 1) >= 0) {
                        mQuantityEditText.setText(String.valueOf(modifyQuantity - 1));
                    } else {
                        Toast.makeText(EditorActivity.this, R.string.less_than_one_forbidden, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the game's category.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categorySpinnerAdapter);

        // Set the integer mSelected to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.category_party_game))) {
                        mCategory = GamesEntry.CATEGORY_PARTY_GAME; // Party Game
                    } else if (selection.equals(getString(R.string.category_american))) {
                        mCategory = GamesEntry.CATEGORY_AMERICAN; // American
                    } else if (selection.equals(getString(R.string.category_german))) {
                        mCategory = GamesEntry.CATEGORY_GERMAN; // German
                    } else {
                        mCategory = GamesEntry.CATEGORY_OTHER; // Other
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = GamesEntry.CATEGORY_OTHER; // Other
            }
        });
    }

    /**
     * Get user input from editor and save game into database.
     */
    private void saveGame() {
        // Read from input fields and use trim to eliminate leading or trailing white space
        String titleString = mTitleEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierContactsString = mSupplierContactsEditText.getText().toString().trim();

        // Check if this is supposed to be a new item and if all the fields in the editor are blank.
        if (mCurrentGameUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierContactsString) && TextUtils.isEmpty(quantityString)
                && mCategory == GamesEntry.CATEGORY_OTHER) {
            // Check if all the fields the editor are blank. A toast message appears and suggest to fill the empty fields
            Toast.makeText(this, (getString(R.string.fill_empty_fields)), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(titleString)) {
            mTitleEditText.setError(getString(R.string.introduce_game_title));
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            mPriceEditText.setError(getString(R.string.introduce_game_price));
            return;
        }

        if (TextUtils.isEmpty(quantityString)) {
            mQuantityEditText.setError(getString(R.string.introduce_game_quantity));
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            mSupplierNameEditText.setError(getString(R.string.introduce_supplier_name));
            return;
        }

        if (TextUtils.isEmpty(supplierContactsString)) {
            mSupplierContactsEditText.setError(getString(R.string.introduce_supplier_contacts));
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(GamesEntry.COLUMN_GAME_TITLE, titleString);
        values.put(GamesEntry.COLUMN_GAME_CATEGORY, mCategory);
        values.put(GamesEntry.COLUMN_GAME_PRICE, priceString);
        values.put(GamesEntry.COLUMN_GAME_SUPPLIER_NAME, supplierNameString);
        values.put(GamesEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER, supplierContactsString);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(GamesEntry.COLUMN_GAME_QUANTITY, quantity);

        // Determine if this is a new or existing game by checking if mCurrentGameUri is null or not
        if (mCurrentGameUri == null) {
            // Insert a new game into the provider and return the content URI for the game.
            Uri newUri = getContentResolver().insert(GamesEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_game_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion is successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_game_successful),
                        Toast.LENGTH_LONG).show();
            }

        } else {
            // Otherwise this is an existing game, so update the game with content URI
            int rowsAffected = getContentResolver().update(mCurrentGameUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_game_successful),
                        +Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file. This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentGameUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save game to database
                saveGame();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the game hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the game hasn't changed, continue with handling back button press
        if (!mGameHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog saying that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all game attributes, define a projection that contains
        // all columns from the games table
        String[] projection = {
                GamesEntry._ID,
                GamesEntry.COLUMN_GAME_TITLE,
                GamesEntry.COLUMN_GAME_PRICE,
                GamesEntry.COLUMN_GAME_CATEGORY,
                GamesEntry.COLUMN_GAME_QUANTITY,
                GamesEntry.COLUMN_GAME_SUPPLIER_NAME,
                GamesEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentGameUri,         // Query the content URI for the current game
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of game attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_TITLE);
            int categoryColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_CATEGORY);
            int priceColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(GamesEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            final String title = cursor.getString(titleColumnIndex);
            int category = cursor.getInt(categoryColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierContacts = cursor.getString(supplierPhoneNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierContactsEditText.setText(supplierContacts);

            // Category is a dropdown spinner, so map the constant value from the database
            switch (category) {
                case GamesEntry.CATEGORY_PARTY_GAME:
                    mCategorySpinner.setSelection(1);
                    break;
                case GamesEntry.CATEGORY_AMERICAN:
                    mCategorySpinner.setSelection(2);
                    break;
                case GamesEntry.CATEGORY_GERMAN:
                    mCategorySpinner.setSelection(3);
                    break;
                default:
                    mCategorySpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierContactsEditText.setText("");
        mCategorySpinner.setSelection(0);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this game
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the game.
                deleteGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the game in the database.
     */
    private void deleteGame() {
        // Only perform the delete if this is an existing boardgame.
        if (mCurrentGameUri != null) {
            // Call the ContentResolver to delete the game at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentGameUri
            // content URI already identifies the game that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentGameUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}