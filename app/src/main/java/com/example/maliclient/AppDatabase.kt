package com.example.maliclient

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.maliclient.dao.MessageDao
import com.example.maliclient.dao.UserDao
import com.example.maliclient.dao.UserKeysDbDao
import com.example.maliclient.model.MessageDb
import com.example.maliclient.model.User
import com.example.maliclient.model.UserKeysDb
import kotlin.reflect.KClass

@Database(entities = [User::class, MessageDb::class, UserKeysDb::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun userkeysDao(): UserKeysDbDao
}
