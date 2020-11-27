package com.example.maliclient.model

class UserCard {
    var id : Long = -1
    var login: String = ""
    var is_add : Boolean = false

    constructor(id : Long, login : String) {
        this.id = id
        this.login = login
    }

    constructor(is_add : Boolean) {
        this.is_add = is_add
    }

    constructor()
}