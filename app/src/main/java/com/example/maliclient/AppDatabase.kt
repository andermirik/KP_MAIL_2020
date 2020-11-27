package com.example.maliclient

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.maliclient.dao.UserDao
import com.example.maliclient.model.User
import kotlin.reflect.KClass

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
