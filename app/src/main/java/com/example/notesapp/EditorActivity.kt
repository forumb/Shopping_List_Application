package com.example.notesapp

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.notesapp.data.ItemContract.ItemEntry


class EditorActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {

    /** EditText field to enter the item's name  */
    private lateinit var mNameEditText: EditText

    /** EditText field to enter the item's description  */
    private lateinit var mDescriptionEditText: EditText

    /** EditText field to enter the item's quantity  */
    private lateinit var mQuantityEditText: EditText

    /** EditText field to enter the item's category  */
    private lateinit var mCategorySpinner: Spinner

    /**
     * Category of the item. The possible values are:
     * 0 for other category, 1 for food, 2 for diary.
     */
    private var mCategory = 0
    private  var mItemHasChanged: Boolean = false

    private var editItemUri: Uri? = null

    private val EXISTING_ITEM_LOADER: Int = 0
    val TAG = EditorActivity::class.java.simpleName

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mItemHasChanged boolean to true.
    val mTouchListener = View.OnTouchListener { view, motionEvent ->
        mItemHasChanged = true
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        val intent: Intent = getIntent()
        editItemUri = intent.data

        Log.d(TAG, "Uri is : " + editItemUri)
        if(editItemUri != null) {
            setTitle(R.string.edit_item_title_text)
            this.supportLoaderManager.initLoader(EXISTING_ITEM_LOADER, null, this)

        }
        else {
            setTitle(R.string.new_item_title_text)
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu()
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_item_name)
        mDescriptionEditText = findViewById(R.id.edit_item_description)
        mQuantityEditText = findViewById(R.id.edit_item_quantity)
        mCategorySpinner = findViewById(R.id.spinner_category)

        setupSpinner()

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener)
        mDescriptionEditText.setOnTouchListener(mTouchListener)
        mQuantityEditText.setOnTouchListener(mTouchListener)
        mCategorySpinner.setOnTouchListener(mTouchListener)


    }



    private fun showUnsavedChangesDialog(discardButtonClickListener: DialogInterface.OnClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
        builder.setPositiveButton(R.string.discard, discardButtonClickListener)
        builder.setNegativeButton(R.string.keep_editing
        ) { dialog, id -> // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the item.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onBackPressed() {
        if(!mItemHasChanged) {
            super.onBackPressed()
            return
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        val discardButtonClickListener =
            DialogInterface.OnClickListener { dialogInterface, i -> // User clicked "Discard" button, close the current activity.
                finish()
            }
        showUnsavedChangesDialog(discardButtonClickListener)
    }

    /**
     * Setup the dropdown spinner that allows the user to select the category of the item.
     */
    private fun setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        val categorySpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this,
            R.array.array_category_options, android.R.layout.simple_spinner_item
        )

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        // Apply the adapter to the spinner
        mCategorySpinner.adapter = categorySpinnerAdapter

        // Set the integer mSelected to the constant values
        mCategorySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selection = parent.getItemAtPosition(position) as String
                if (!TextUtils.isEmpty(selection)) {
                    mCategory = if (selection == getString(R.string.category_food)) {
                        1 // Food
                    } else if (selection == getString(R.string.category_diary)) {
                        2 // Diary
                    } else {
                        0 // Other
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            override fun onNothingSelected(parent: AdapterView<*>?) {
                mCategory = 0 // Unknown
            }
        }
    }

    //Get user input from edittext and save into database
    private fun saveItem(){

            val nameString: String = mNameEditText.text.toString().trim()
            val descriptionString: String = mDescriptionEditText.text.toString().trim()
            var quantity: Int = 0
            val quantityString: String = mQuantityEditText.text.toString()
        if(!TextUtils.isEmpty(quantityString)){
            quantity = Integer.parseInt(quantityString)
        }


        if(editItemUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(descriptionString) &&
            mCategory == ItemEntry.CATEGORY_OTHER) {
            return
        }


            val values = ContentValues()
            values.put(ItemEntry.COLUMN_ITEM_NAME, nameString)
            values.put(ItemEntry.COLUMN_ITEM_DESCRIPTION, descriptionString)
            values.put(ItemEntry.COLUMN_ITEM_CATEGORY, mCategory)
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity)

        if (editItemUri == null){
            val uri: Uri? = contentResolver.insert(ItemEntry.CONTENT_URI, values)

            if (uri != null) {
 //               Toast.makeText(this, "Item added successfully with uri $uri", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Item added successfully with uri: "+ uri)
            } else
//                Toast.makeText(this, "Failed to add new item $uri", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Failed to add new item with uri: "+ uri)
        }
        else{
        val updatedRow: Int = contentResolver.update(editItemUri!!, values, null, null)

        if (updatedRow != 0) {
//            Toast.makeText(this, "Item at updated successfully", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Item updated successfully with uri "+ updatedRow)
        } else
//            Toast.makeText(this, "Failed to update item", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Failed to update item with uri "+ updatedRow)
    }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_editor, menu)
        if(editItemUri == null){
            val menuItem: MenuItem = menu!!.findItem(R.id.action_delete)
            menuItem.setVisible(false)
        }
        return true
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
//    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        return super.onPrepareOptionsMenu(menu)
//        if(editItemUri == null){
//            val menuItem: MenuItem = menu!!.findItem(R.id.action_delete)
//            menuItem.setVisible(false)
//        }
//        return true
//    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            R.id.action_save ->  {
                //Save Item to database
                saveItem()
                //Exit activity
                finish()
                return true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                return true}
            android.R.id.home -> {
                if(!mItemHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this)
                    return true
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should be discarded.
                val discardButtonClickListener =
                    DialogInterface.OnClickListener { dialogInterface, i -> // User clicked "Discard" button, close the current activity.
                        NavUtils.navigateUpFromSameTask(this)
                    }
                showUnsavedChangesDialog(discardButtonClickListener)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        //This selects the data we are interested to set in edit text
        val mProjection = arrayOf(
            ItemEntry._ID,
            ItemEntry.COLUMN_ITEM_NAME,
            ItemEntry.COLUMN_ITEM_QUANTITY,
            ItemEntry.COLUMN_ITEM_DESCRIPTION,
            ItemEntry.COLUMN_ITEM_CATEGORY
        )

        //This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(this, editItemUri!!, mProjection, null, null, null)

    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {

        if (cursor != null) {
            if (cursor.count <= 0) {
                return
            }
            if (cursor.moveToFirst()) {
                val nameColumnIndex: Int = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME)
                val descriptionColumnIndex: Int = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_DESCRIPTION)
                val categoryColumnIndex: Int = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_CATEGORY)
                val quantityColumnIndex: Int = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY)

                // Extract out the value from the Cursor for the given column index

                // Extract out the value from the Cursor for the given column index
                val name: String = cursor.getString(nameColumnIndex)
                val description: String = cursor.getString(descriptionColumnIndex)
                val category: Int = cursor.getInt(categoryColumnIndex)
                val quantity: Int = cursor.getInt(quantityColumnIndex)

                Log.d("EditorActivity", "Name: $name, Description: $description, Category: $category, Qantity: $quantity")

                mNameEditText.setText(name)
                mQuantityEditText.setText(quantity.toString())
                mDescriptionEditText.setText(description)

                // Category is a dropdown spinner, so map the constant value from the database
                // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
                // Then call setSelection() so that option is displayed on screen as the current selection.
                when (category) {
                    ItemEntry.CATEGORY_FOOD -> mCategorySpinner.setSelection(1)
                    ItemEntry.CATEGORY_DIARY -> mCategorySpinner.setSelection(2)
                    else -> mCategorySpinner.setSelection(0)
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mQuantityEditText.setText("");
        mCategorySpinner.setSelection(0); // Select "Other" category
    }

    private fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        val builder =
            AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.delete
        ) { dialog, id -> // User clicked the "Delete" button, so delete the item.
            deleteItem()
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
     * Perform the deletion of the item in the database.
     */
    private fun deleteItem() {
        if (editItemUri != null){
            val rowsDeleted: Int = contentResolver.delete(editItemUri!!, null, null)

            if (rowsDeleted != 0) {
                Toast.makeText(this, R.string.editor_delete_item_successful, Toast.LENGTH_LONG)
                    .show()
            } else
                Toast.makeText(this, R.string.editor_delete_item_failed, Toast.LENGTH_LONG).show()
        }

        finish()
    }

}
