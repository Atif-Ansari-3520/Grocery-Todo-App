package com.example.todoapp.Database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(time: Long?): Date? = time?.let { Date(it) }
}
