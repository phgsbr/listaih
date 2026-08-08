package com.listaih.app.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun fromDate(date: java.util.Date?): Long? {
        return date?.time
    }
}