package com.example.maliclient.model

import java.util.*

class MessageCard{
    var sender_name = ""
    var sender_address = ""
    var subject = ""
    var body = ""
    var date: Date = Date()
    var message_uid: Long = 0L
    var folder_name = ""
    var userlogin = ""
    var isForDate = false
    var isReaded = false


    constructor(body: String, isForDate : Boolean) {
        this.body = body
        this.isForDate = isForDate
    }

    constructor(sender_name: String, sender_address: String, subject: String, body: String, date: Date, message_uid: Long, folder_name : String, userlogin : String, isReaded : Boolean) {
        this.sender_name = sender_name
        this.sender_address = sender_address
        this.subject = subject
        this.body = body
        this.date = date
        this.message_uid = message_uid
        this.folder_name = folder_name
        this.userlogin = userlogin
        this.isReaded = isReaded
    }

    constructor()
}