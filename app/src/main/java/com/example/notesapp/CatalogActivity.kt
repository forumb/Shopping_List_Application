package com.example.notesapp

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.notesapp.data.ItemContract.ItemEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CatalogActivity : AppCompatActivity(), LoaderCallbacks<Cursor>{

    lateinit var itemCursorAdapter:ItemCursorAdapter
    val ITEM_LOADER: Int = 0
    val TAG = CatalogActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Setup FAB to open EditorActivity
        val fab: FloatingActionButton = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
            startActivity(intent)
        })



        val itemListView = findViewById<ListView>(R.id.list_view_items)
        val emptyView = findViewById<View>(R.id.empty_view)

        itemListView.emptyView = emptyView

       itemCursorAdapter = ItemCursorAdapter(this, null)
        itemListView.adapter = itemCursorAdapter

        itemListView.setOnItemClickListener{ parent, view, position, id ->
        val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
            val uri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id)
            intent.data = uri
            startActivity(intent)
        }

        //Init the loader
        this.supportLoaderManager.initLoader(ITEM_LOADER, null, this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
//            R.id.action_insert_dummy_data -> {
//                return true
//            }
            R.id.action_delete_all_entries -> {
                showDeleteConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        //This selects the data we are interested to display
        val mProjection = arrayOf(
            ItemEntry._ID,
            ItemEntry.COLUMN_ITEM_NAME,
            ItemEntry.COLUMN_ITEM_DESCRIPTION,
            ItemEntry.COLUMN_ITEM_QUANTITY
        )

        //This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(this,ItemEntry.CONTENT_URI, mProjection, null, null, null)


    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        itemCursorAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        itemCursorAdapter.swapCursor(null)
    }

    private fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        val builder =
            AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_all_dialog_msg)
        builder.setPositiveButton(R.string.delete
        ) { dialog, id -> // User clicked the "Delete" button, so delete the item.
            deleteAllItems()
        }
        builder.setNegativeButton(R.string.cancel
        ) { dialog, id -> // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the item.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * Perform the deletion of the items in the database.
     */
    private fun deleteAllItems() {
            val rowsDeleted: Int = contentResolver.delete(ItemEntry.CONTENT_URI, null, null)

            if (rowsDeleted != 0) {
                Toast.makeText(this, R.string.editor_delete_all_items_successful, Toast.LENGTH_LONG)
                    .show()
            } else
                Toast.makeText(this, R.string.editor_delete_item_failed, Toast.LENGTH_LONG).show()


    }
}