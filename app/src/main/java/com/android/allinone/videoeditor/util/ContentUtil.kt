package com.android.allinone.videoeditor.util

import android.database.Cursor

object ContentUtil {
    fun getLong(cursor: Cursor): String {
        StringBuilder().apply {
            append("")
            append(cursor.getLong(cursor.getColumnIndexOrThrow("_id")))
            return toString()
        }
    }

    fun getTime(cursor: Cursor, str: String?): String {
        return TimeUtils.toFormattedTime(getInt(cursor, str))
    }

    private fun getInt(cursor: Cursor, str: String?): Int {
        return cursor.getInt(cursor.getColumnIndexOrThrow(str))
    }
}