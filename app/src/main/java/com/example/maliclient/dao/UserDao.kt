package com.example.maliclient.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.maliclient.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE login LIKE :user_login")
    fun getByLogin(user_login : String) : List<User>

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)

}