package com.example.maliclient.adapter

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.maliclient.AppDatabase
import com.example.maliclient.R
import com.example.maliclient.model.User
import com.example.maliclient.model.UserKeysDb
import java.io.IOException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.internet.MimeUtility


class AttachmentAdapter(var context: Context, var attachments: Array<BodyPart>, var folder : Folder, var username: String)
    : RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    var colors = mapOf(
        "docx" to Color.parseColor("#2B569A"),
        "pdf" to Color.parseColor("#E55B5B"),
        "txt" to Color.parseColor("#32ADD9"),
        "png" to Color.parseColor("#6995D6"),
        "jpg" to Color.parseColor("#6995D6"),
        "jpeg" to Color.parseColor("#6995D6"),
        "gif" to Color.parseColor("#6995D6"),
        "key" to Color.parseColor("#FFFFC107"),
        "else" to Color.parseColor("#A5A4A2")
    )
    var num = RecyclerView.NO_POSITION
    fun select(num : Int){
        if (num == RecyclerView.NO_POSITION)
            return

        if(attachments[num].fileName=="public-${username}.key"){
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "database"
            )
                .allowMainThreadQueries()
                .build()

            if(db.userkeysDao().getByLogin(username).isEmpty()) {
                val buf = ByteArray(attachments[num].size)
                attachments[num].inputStream.read(buf, 0, attachments[num].size)

                val X509publicKey = X509EncodedKeySpec(buf)
                val kf: KeyFactory = KeyFactory.getInstance("RSA")
                val public = kf.generatePublic(X509publicKey)

                db.userkeysDao().insertAll(UserKeysDb(username, public.encoded, byteArrayOf()))
                Toast.makeText(context, "ключ сохранён", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, "ключ уже сохранён", Toast.LENGTH_SHORT).show()
            }
        }else{
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            val thread = Thread(Runnable {
                if(!folder.isOpen)
                    folder.open(Folder.READ_ONLY)
                intent.type = attachments[num].contentType
                intent.putExtra(Intent.EXTRA_TITLE, MimeUtility.decodeText(attachments[num].fileName))
                this.num = num
                (context as Activity).startActivityForResult(intent,1)
            })
            thread.start()
            thread.join()
        }
    }

    fun on_result(requestCode: Int, resultCode: Int, data: Intent?){
        if(requestCode == 1 && resultCode == RESULT_OK){
            if(num == RecyclerView.NO_POSITION)
                return
            val thread = Thread(Runnable {
                try {
                    if(!folder.isOpen)
                        folder.open(Folder.READ_ONLY)
                    val uri: Uri = data!!.data!!

                    val outputStream = context.contentResolver.openOutputStream(uri)
                    val buf = ByteArray(attachments[num].size)
                    //var length: Int
                    attachments[num].inputStream.read(buf)
                    //while (attachments[num].inputStream.read(buf).also { length = it } > 0) {
                        outputStream!!.write(buf, 0, attachments[num].size)
                    //}
                    outputStream!!.close()
                }catch(e: IOException){

                }
            })
            thread.start()
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var card_file = view.findViewById<CardView>(R.id.card_file)
        var card_format = view.findViewById<CardView>(R.id.card_format)
        var card_image = view.findViewById<CardView>(R.id.card_image)
        var card_attachment = view.findViewById<ConstraintLayout>(R.id.card_attachment)

        var tv_filename = view.findViewById<TextView>(R.id.tv_filename)
        var tv_format = view.findViewById<TextView>(R.id.tv_format)
        var tv_size = view.findViewById<TextView>(R.id.tv_size)

        var image_image = view.findViewById<ImageView>(R.id.image_image)

        init{
            card_attachment.setOnClickListener{
                select(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_attachment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return attachments.count()
    }

    fun convert_size(bytes: Long) : String{
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return "$bytes B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte).toString() + " КБ";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte).toString() + " МБ";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte).toString() + " ГБ";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte).toString() + " ТБ";

        } else {
            return bytes.toString() + " Б"
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val attachment = attachments[position]
        var format = ""
        var color = "else"
        var delim: List<String> = listOf()
        var bitmap : Bitmap? = null
        var is_img = false

        val thread = Thread(Runnable {
            delim = MimeUtility.decodeText(attachment.fileName).split('.')
            if(delim.isNotEmpty()) {
                format = delim[delim.size-1]
                if(format in colors.keys)
                    color = format
            }

            if(!folder.isOpen)
                folder.open(Folder.READ_ONLY)

            is_img = format in arrayOf("png", "jpg", "bmp", "jpeg", "gif")

            if(is_img){
                bitmap = BitmapFactory.decodeStream(attachment.inputStream)
            }
        })

        thread.start()
        thread.join()

        if(format!= "")
            holder.tv_format.text = format.toUpperCase()
        else
            holder.card_format.visibility = View.GONE

        if(is_img && bitmap != null){
            holder.image_image.setImageBitmap(bitmap)
            holder.card_image.visibility = View.VISIBLE
            holder.card_file.visibility = View.GONE

        }else {
            holder.card_image.visibility = View.GONE
            holder.tv_filename.text = delim.subList(0, delim.size - 1)
                    .joinToString(separator = ".") { it -> it }
            holder.tv_size.text = convert_size(attachment.size.toLong())
        }

        holder.card_format.setCardBackgroundColor(colors[color]!!)
    }
}
