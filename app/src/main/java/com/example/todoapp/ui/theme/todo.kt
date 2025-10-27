package com.example.todoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val quantity: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDone: Boolean = false
)
