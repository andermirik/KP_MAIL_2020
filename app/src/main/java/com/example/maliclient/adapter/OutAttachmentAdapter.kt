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
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.maliclient.R
import com.example.maliclient.SendMailActivity
import kotlinx.android.synthetic.main.item_attachment.view.*
import java.io.IOException
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.internet.MimeUtility


class OutAttachmentAdapter(var context: Context, var attachments: Array<BodyPart>, var act: SendMailActivity)
    : RecyclerView.Adapter<OutAttachmentAdapter.ViewHolder>() {

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
        act.on_delete(num)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var card_file = view.findViewById<CardView>(R.id.card_file)
        var card_format = view.findViewById<CardView>(R.id.card_format)
        var card_image = view.findViewById<CardView>(R.id.card_image)
        var card_delete = view.findViewById<CardView>(R.id.card_delete)

        var card_attachment = view.findViewById<ConstraintLayout>(R.id.card_attachment)

        var tv_filename = view.findViewById<TextView>(R.id.tv_filename)
        var tv_format = view.findViewById<TextView>(R.id.tv_format)
        var tv_size = view.findViewById<TextView>(R.id.tv_size)

        var image_image = view.findViewById<ImageView>(R.id.image_image)

        init{
            card_delete.image_close.setOnClickListener{
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
                if(format.contains(' '))
                    format = format.split(" ")[0]
                if(format in colors.keys)
                    color = format
            }


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
            holder.tv_size.text = convert_size(attachment.inputStream.available().toLong())
        }

        holder.card_delete.visibility = View.VISIBLE
        holder.card_format.setCardBackgroundColor(colors[color]!!)
    }
}
