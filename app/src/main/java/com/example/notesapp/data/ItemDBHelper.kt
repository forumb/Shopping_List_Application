package com.example.notesapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ItemDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ITEMS_TABLE: String =  "CREATE TABLE " + ItemContract.ItemEntry.TABLE_NAME + " (" + ItemContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ ItemContract.ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "+ ItemContract.ItemEntry.COLUMN_ITEM_DESCRIPTION + " TEXT, "+ ItemContract.ItemEntry.COLUMN_ITEM_CATEGORY + " INTEGER NOT NULL, "+ ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0);"

        db.execSQL(SQL_CREATE_ITEMS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

    companion object {
        const val DATABASE_NAME = "shopping_list.db"
        const val DATABASE_VERSION: Int = 1

    }
}