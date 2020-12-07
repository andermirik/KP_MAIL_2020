package com.example.maliclient.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class UserKeysDb {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var user_login = ""
    var current_user_login = ""
    var public_key = byteArrayOf()
    var private_key = byteArrayOf()

    constructor(user_login: String, current_user_login:String, public_key: ByteArray, private_key: ByteArray) {
        this.user_login = user_login
        this.current_user_login = current_user_login
        this.public_key = public_key
        this.private_key = private_key
    }

    constructor()
}