package com.example.maliclient.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.maliclient.model.UserKeysDb

@Dao
interface UserKeysDbDao {
    @Query("SELECT * FROM userkeysdb")
    fun getAll(): List<UserKeysDb>

    @Query("SELECT * FROM userkeysdb WHERE user_login LIKE :user_login AND current_user_login LIKE :curent_user_login")
    fun getByLogin(user_login : String, curent_user_login: String) : List<UserKeysDb>

    @Insert
    fun insertAll(vararg user_keys: UserKeysDb)

    @Delete
    fun delete(user: UserKeysDb)

}