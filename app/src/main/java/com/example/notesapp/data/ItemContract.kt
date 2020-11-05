package com.example.notesapp.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

public final class ItemContract {

    class ItemEntry : BaseColumns {

        companion object {
            const val TABLE_NAME = "shopping_list"
            const val _ID = BaseColumns._ID
            const val COLUMN_ITEM_NAME = "name"
            const val COLUMN_ITEM_DESCRIPTION = "description"
            const val COLUMN_ITEM_CATEGORY = "category"
            const val COLUMN_ITEM_QUANTITY = "quantity"

            const val CONTENT_AUTHORITY = "com.example.android.notesapp"
            val BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY)
            const val PATH_ITEM = "items"
            val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEM)

            val CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM
            val CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM

            const val CATEGORY_OTHER = 0
            const val CATEGORY_FOOD = 1
            const val CATEGORY_DIARY = 2

            fun isValidCategory(category: Int): Boolean{
                return category == CATEGORY_OTHER || category == CATEGORY_DIARY || category == CATEGORY_FOOD
            }
        }
    }

    companion object {
        const val CONTENT_AUTHORITY = "com.example.android.notesapp"
        const val PATH_ITEMS = "items"
    }

}