package com.example.maliclient.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.maliclient.MainActivity
import com.example.maliclient.R
import com.example.maliclient.model.AttachmentCard
import com.example.maliclient.model.MessageCard
import java.lang.Exception
import java.util.*
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.internet.MimeUtility
import kotlin.concurrent.thread
import kotlin.random.Random

class AttachmentAdapter(var context: Context, var attachments: Array<BodyPart>, var folder : Folder)
    : RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    var colors = mapOf(
        "docx" to Color.parseColor("#2B569A"),
        "pdf" to Color.parseColor("#E55B5B"),
        "txt" to Color.parseColor("#32ADD9"),
        "png" to Color.parseColor("#6995D6"),
        "jpg" to Color.parseColor("#6995D6"),
        "jpeg" to Color.parseColor("#6995D6"),
        "gif" to Color.parseColor("#6995D6"),
        "else" to Color.parseColor("#A5A4A2")
    )

    fun select(num : Int){
        if (num == RecyclerView.NO_POSITION)
            return


        //main_activity.on_select_message(messages[num].message_uid)
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

    fun convert_size(size: Int) : String{
        return "13,2 МБ"
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
            holder.tv_size.text = convert_size(attachment.size)
        }

        holder.card_format.setCardBackgroundColor(colors["else"]!!)
    }
}
