package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_BREED;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_GENDER;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_NAME;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_WEIGHT;
import static com.example.android.pets.data.PetsContract.PetEntry.CONTENT_URI;
import static com.example.android.pets.data.PetsContract.PetEntry.GENDER_MALE;
import static com.example.android.pets.data.PetsContract.PetEntry._ID;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Loader indetifier
    private static final int URL_LOADER = 0;
    private static final String TAG = CatalogActivity.class.getClass().getSimpleName();

    private ActionMode mActionMode;
    private long itemId;

    public ActionMode.Callback mCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_long_click, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // User clicked on a menu option in the app bar overflow menu
            switch (item.getItemId()) {
                // Respond to a click on the "Save" menu option
                case R.id.action_edit:
                    // Saving pets info to the database
                    Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                    intent.setData(ContentUris.withAppendedId(CONTENT_URI, itemId));
                    startActivity(intent);
                    mode.finish();
                    return true;
                // Respond to a click on the "Delete" menu option
                case R.id.action_delete:
                    //Delete pet from the database
                    showDeleteConfirmationDialog();
                    mode.finish();
                    return true;
                default:
                    return false;
            }

        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    // Empty adapter
    PetCursorAdapter adapter = new PetCursorAdapter(this, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        Stetho.initializeWithDefaults(this);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
//                intent.setData(ContentUris.withAppendedId(CONTENT_URI, itemId));
                startActivity(intent);
            }
        });

        // Initialize loader
        getLoaderManager().initLoader(URL_LOADER, null, this);

        // Find ListView to populate
        ListView petsList = findViewById(R.id.pets_list_view);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        petsList.setEmptyView(findViewById(R.id.empty_view));

        // Attach cursor adapter to the ListView
        petsList.setAdapter(adapter);

        //Click listener
        petsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) return false;
                // Start the contextual action bar using the ActionMode.Callback.
                mActionMode = CatalogActivity.this.startActionMode(mCallback);
                itemId = id;
                view.setSelected(true);
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                _ID,
                COLUMN_PET_NAME,
                COLUMN_PET_BREED};

        return new CursorLoader(
                this,
                CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        adapter.swapCursor(null);
    }

    private void insertPet() {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_PET_NAME, "Tommy");
        values.put(COLUMN_PET_BREED, "Pomeranian");
        values.put(COLUMN_PET_GENDER, GENDER_MALE);
        values.put(COLUMN_PET_WEIGHT, 4);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(CONTENT_URI, values);

        // Display success/failed snackbar message
        Snackbar.make(findViewById(R.id.catalog_layout), (newUri != null) ?
                getString(R.string.dummy_insertion_success) :
                getString(R.string.dummy_insetion_failed), Snackbar.LENGTH_SHORT).show();

    }

    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
     * Perform the deletion of the all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(CONTENT_URI, null, null);

        // Display success/failed toast message
//        Toast.makeText(this, (rowsDeleted != 0) ?
//                        (getString(R.string.editor_delete_all_pets_successful)) :
//                        getString(R.string.editor_delete_pet_failed),
//                Toast.LENGTH_SHORT).show();

        // Display success/failed snackbar message
        Snackbar.make(findViewById(R.id.catalog_layout), (rowsDeleted != 0) ?
                (getString(R.string.editor_delete_all_pets_successful)) :
                getString(R.string.editor_delete_pet_failed), Snackbar.LENGTH_SHORT).show();

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        int rowsDeleted = getContentResolver()
                .delete(ContentUris.withAppendedId(CONTENT_URI, itemId),null, null);

        // Display success/failed snackbar message
        Snackbar.make(findViewById(R.id.catalog_layout), (rowsDeleted != 0) ?
                (getString(R.string.editor_delete_pet_successful)) :
                getString(R.string.editor_delete_pet_failed), Snackbar.LENGTH_SHORT).show();

    }
}