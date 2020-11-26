package com.example.maliclient.model

class UserCard {
    var login: String = ""
    var is_add : Boolean = false

    constructor(login : String) {
        this.login = login
    }

    constructor(is_add : Boolean) {
        this.is_add = is_add
    }

    constructor()
}