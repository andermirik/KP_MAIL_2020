package com.example.maliclient.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.maliclient.model.MessageDb
import com.example.maliclient.model.User

@Dao
interface MessageDao {
    @Query("SELECT * FROM messagedb")
    fun getAll(): List<MessageDb>

    @Query("SELECT * FROM messagedb WHERE message_uid = :uid AND folder_name = :folder_name AND userlogin = :username")
    fun getByUidAndFolderNameAndUserName(uid: Long, folder_name: String, username: String) : List<MessageDb>

    @Query("SELECT * FROM messagedb WHERE folder_name = :folder_name AND userlogin = :username")
    fun getBydFolderNameAndUserName(folder_name: String, username: String) : List<MessageDb>

    @Insert
    fun insertAll(vararg messages: MessageDb)

    @Delete
    fun delete(message: MessageDb)

}