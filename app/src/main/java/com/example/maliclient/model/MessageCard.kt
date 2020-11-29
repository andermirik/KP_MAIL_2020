package com.example.maliclient.model

import java.util.*

class MessageCard{
    var sender_name = ""
    var subject = ""
    var body = ""
    var date: Date = Date()
    var message_uid: Long = 0L
    var folder_name = ""
    var userlogin = ""

    constructor(sender_name: String, subject: String, body: String, date: Date, message_uid: Long, folder_name : String, userlogin : String) {
        this.sender_name = sender_name
        this.subject = subject
        this.body = body
        this.date = date
        this.message_uid = message_uid
        this.folder_name = folder_name
        this.userlogin = userlogin
    }

    constructor()
}