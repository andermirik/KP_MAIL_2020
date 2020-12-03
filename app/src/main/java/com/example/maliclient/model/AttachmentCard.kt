package com.example.maliclient.model

import java.io.InputStream

class AttachmentCard {
    var size : Int = 0
    var filename=""
    var input_stream : InputStream? = null

    constructor(filename: String, size: Int, inputStream: InputStream) {
        this.filename = filename
        this.size = size
        this.input_stream = input_stream
    }

    constructor()
}