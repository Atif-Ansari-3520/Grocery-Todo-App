package com.example.todoapp

import android.app.Application

class MainApplication : Application() {
    val database: TodoDatabase by lazy {
        TodoDatabase.getDatabase(this)
    }
}
