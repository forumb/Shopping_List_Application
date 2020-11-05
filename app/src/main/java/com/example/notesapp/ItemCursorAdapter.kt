package com.example.notesapp

import android.content.Context
import android.database.Cursor
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.example.notesapp.data.ItemContract


class ItemCursorAdapter(context: Context?, c: Cursor?) : CursorAdapter(context, c) {

    override fun newView(context: Context?, c: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
    }

    override fun bindView(view: View?, context: Context?, c: Cursor?) {

        val nameTv: TextView = view?.findViewById(R.id.name) as TextView
        val summaryTv: TextView = view.findViewById(R.id.summary) as TextView
        val quantityTv: TextView = view.findViewById(R.id.quantity) as TextView

        if (c != null) {
            val name: String =
                c.getString(c.getColumnIndexOrThrow(ItemContract.ItemEntry.COLUMN_ITEM_NAME))
            nameTv.setText(name)

            val summary: String = c.getString(c.getColumnIndexOrThrow(ItemContract.ItemEntry.COLUMN_ITEM_DESCRIPTION))
            if(TextUtils.isEmpty(summary))
                summaryTv.setText(R.string.empty_description)
            else summaryTv.setText(summary)

            val quantity:Int = c.getInt(c.getColumnIndexOrThrow(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY))
            if(TextUtils.isEmpty(quantity.toString()))
                quantityTv.setText("0")
            else quantityTv.setText(quantity.toString())
        }


    }
}