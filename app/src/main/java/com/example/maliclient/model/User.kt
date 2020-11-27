package com.example.maliclient.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity()
class User {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var login: String = ""
    var password = ""

    var imap_server = ""
    var imap_port = 0

    var smtp_login = ""
    var smtp_port = 0

    var enable_ssl = false

    constructor()

    constructor(
        login: String,
        password: String,
        imap_server: String,
        imap_port: Int,
        smtp_login: String,
        smtp_port: Int,
        enable_ssl: Boolean
    ) {
        this.login = login
        this.password = password
        this.imap_server = imap_server
        this.imap_port = imap_port
        this.smtp_login = smtp_login
        this.smtp_port = smtp_port
        this.enable_ssl = enable_ssl
    }
}