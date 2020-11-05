package com.example.notesapp.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.example.notesapp.data.ItemContract.ItemEntry

class ItemProvider : ContentProvider() {
    companion object {
        /** Tag for the log messages  */
        val LOG_TAG = ItemProvider::class.java.simpleName

        /** URI matcher code for the content URI for the shopping list table  */
        private const val ITEMS = 100

        /** URI matcher code for the content URI for a single item in the shopping list table  */
        private const val ITEM_ID = 101

        /**
         * UriMatcher object to match a content URI to a corresponding code.
         * The input passed into the constructor represents the code to return for the root URI.
         * It's common to use NO_MATCH as the input for this case.
         */
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        // Static initializer. This is run the first time anything is called from this class.
        init {
            // The calls to addURI() go here, for all of the content URI patterns that the provider
            // should recognize. All paths added to the UriMatcher have a corresponding code to return
            // when a match is found.

            // The content URI of the form "content://com.example.android.notesapp/items" will map to the
            // integer code {@link #ITEMS}. This URI is used to provide access to MULTIPLE rows
            // of the table.
            sUriMatcher.addURI(
                ItemContract.CONTENT_AUTHORITY,
                ItemContract.PATH_ITEMS, ITEMS
            )

            // The content URI of the form "content://com.example.android.notesapp/items/#" will map to the
            // integer code {@link #ITEM_ID}. This URI is used to provide access to ONE single row
            // of the table.
            //
            // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
            // For example, "content://com.example.android.notesapp/items/3" matches, but
            // "content://com.example.android.notesapp/items" (without a number at the end) doesn't match.
            sUriMatcher.addURI(
                ItemContract.CONTENT_AUTHORITY,
                ItemContract.PATH_ITEMS + "/#", ITEM_ID
            )
        }
    }

    /** Database helper object  */
    private var mDbHelper: ItemDBHelper? = null

    override fun onCreate(): Boolean {
        mDbHelper = ItemDBHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        // Get readable database
        var selection = selection
        var selectionArgs = selectionArgs
        val database: SQLiteDatabase = mDbHelper!!.getReadableDatabase()

        // This cursor will hold the result of the query
        val cursor: Cursor

        // Figure out if the URI matcher can match the URI to a specific code
        val match: Int = sUriMatcher.match(uri)
        when (match) {
            ITEMS ->                 // For the ITEMS code, query the shopping list table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the shopping list table.
                cursor = database.query(
                    ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder
                )
            ITEM_ID -> {
                // For the ITEMS_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.notesapp/items/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ItemEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())

                // This will perform a query on the table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(
                    ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder
                )
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(context!!.contentResolver, uri)

        // Return the cursor
        return cursor
    }

    override fun insert(
        uri: Uri,
        contentValues: ContentValues?
    ): Uri? {
        val match: Int = sUriMatcher.match(uri)
        return when (match) {
            ITEMS -> insertItem(uri, contentValues)
            else -> throw IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    /**
     * Insert a item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private fun insertItem(uri: Uri, values: ContentValues?): Uri? {
        // Check that the name is not null
        val name = values!!.getAsString(ItemEntry.COLUMN_ITEM_NAME)
            ?: throw IllegalArgumentException("Item requires a name")

        // Check that the category is valid
        val category = values.getAsInteger(ItemEntry.COLUMN_ITEM_CATEGORY)
        require(!(category == null || !ItemEntry.isValidCategory(category))) { "Item requires valid category" }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        val quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY)
        require(!(quantity != null && quantity < 0)) { "Item requires valid quantity" }

        // No need to check the breed, any value is valid (including null).

        // Get writeable database
        val database: SQLiteDatabase = mDbHelper!!.getWritableDatabase()

        // Insert the new item with the given values
        val id = database.insert(ItemEntry.TABLE_NAME, null, values)
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1L) {
            Log.e(LOG_TAG, "Failed to insert row for $uri")
            return null
        }

        // Notify all listeners that the data has changed for the item content URI
        context!!.contentResolver.notifyChange(uri, null)

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id)
    }

    override fun update(
        uri: Uri, contentValues: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var selection = selection
        var selectionArgs = selectionArgs
        val match: Int = sUriMatcher.match(uri)
        return when (match) {
            ITEMS -> updateItem(
                uri,
                contentValues,
                selection,
                selectionArgs
            )
            ITEM_ID -> {
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ItemEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                updateItem(uri, contentValues, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Update is not supported for $uri")
        }
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private fun updateItem(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        // If the {@link ItemEntry#COLUMN_ITEM_NAME} key is present,
        // check that the name value is not null.
        if (values!!.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            val name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME)
                ?: throw IllegalArgumentException("Item requires a name")
        }

        // If the {@link ItemEntry#COLUMN_ITEM_CATEGORY} key is present,
        // check that the category value is valid.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_CATEGORY)) {
            val category = values.getAsInteger(ItemEntry.COLUMN_ITEM_CATEGORY)
            require(!(category == null || !ItemEntry.isValidCategory(category))) { "Item requires valid category" }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_QUANTITY} key is present,
        // check that the weight value is valid.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            val weight = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY)
            require(!(weight != null && weight < 0)) { "Item requires valid quantity" }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0
        }

        // Otherwise, get writeable database to update the data
        val database: SQLiteDatabase = mDbHelper!!.getWritableDatabase()

        // Perform the update on the database and get the number of rows affected
        val rowsUpdated =
            database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs)

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }

        // Return the number of rows updated
        return rowsUpdated
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        // Get writeable database
        var selection = selection
        var selectionArgs = selectionArgs
        val database: SQLiteDatabase = mDbHelper!!.getWritableDatabase()

        // Track the number of rows that were deleted
        val rowsDeleted: Int
        val match: Int = sUriMatcher.match(uri)
        when (match) {
            ITEMS ->                 // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs)
            ITEM_ID -> {
                // Delete a single row given by the ID in the URI
                selection = ItemEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Deletion is not supported for $uri")
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }

        // Return the number of rows deleted
        return rowsDeleted
    }

    override fun getType(uri: Uri): String? {
        val match: Int = sUriMatcher.match(uri)
        return when (match) {
            ITEMS -> ItemEntry.CONTENT_LIST_TYPE
            ITEM_ID -> ItemEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalStateException("Unknown URI $uri with match $match")
        }
    }
}