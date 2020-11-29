package com.example.maliclient.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.maliclient.MainActivity
import com.example.maliclient.R
import com.example.maliclient.model.MessageCard
import java.util.*
import kotlin.random.Random

class MessageAdapter(var context: Context, var messages: Array<MessageCard>, var main_activity : MainActivity)
    : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    var colors = arrayOf(
        Color.parseColor("#F44336"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#3F51B5"),
        Color.parseColor("#009688"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#FF5722"),
        Color.parseColor("#EF6C00"),
        Color.parseColor("#D84315"),
        Color.parseColor("#37474F")
    )
    var random = Random(0xff2244)

    fun select(num : Int){
        if (num == RecyclerView.NO_POSITION)
            return


        //main_activity.on_select_folder(folders, num)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var inner_circle = view.findViewById<CardView>(R.id.inner_circle)
        var message_card = view.findViewById<ConstraintLayout>(R.id.message_card)
        var tv_message = view.findViewById<TextView>(R.id.tv_message)
        var tv_date = view.findViewById<TextView>(R.id.tv_date)
        var tv_name = view.findViewById<TextView>(R.id.tv_name)
        var tv_subject = view.findViewById<TextView>(R.id.tv_subject)
        var tv_userlogin = view.findViewById<TextView>(R.id.tv_userlogin)

        init{
            message_card.setOnClickListener{
                select(adapterPosition)
                Toast.makeText(context, messages[adapterPosition].message_uid.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mail, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    fun convert_date(date: Date) : String{
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
        cal.time = date
        val year = cal[Calendar.YEAR]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        val hours = cal[Calendar.HOUR_OF_DAY]
        val minutes = cal[Calendar.MINUTE]

        var smonth = ""
        when(month){
            0-> smonth = "янв."
            1 -> smonth = "фев."
            2 -> smonth = "мар."
            3 -> smonth = "апр."
            4 -> smonth = "мая"
            5 -> smonth = "июн."
            6 -> smonth = "июл.."
            7 -> smonth = "авг."
            8 -> smonth = "сент."
            9 -> smonth = "окт."
            10 -> smonth = "нояб."
            11 -> smonth = "дек."
        }

        val sday = if(day > 9) day .toString() else "0${day}"

        val today = Calendar.getInstance()
        today.timeZone = TimeZone.getTimeZone("Europe/Moscow")

        val is_today = year == today[Calendar.YEAR] && month == today[Calendar.MONTH] && day == today[Calendar.DAY_OF_MONTH]
        val is_yesterday = year == today[Calendar.YEAR] && month == today[Calendar.MONTH] && day == today[Calendar.DAY_OF_MONTH]-1

        if(is_today)
            return "${hours}:${minutes}"

        if(is_yesterday)
            return "Вчера"

        if(year == today[Calendar.YEAR]){
            return "${sday} ${smonth}"
        }
        else{
            return "${sday} ${smonth} ${year}г."
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.setIsRecyclable(false)

        val message = messages[position]

        var login_text = ""
        if(message.sender_name.contains('@')){
            val name = message.sender_name.split("@")[0]
            login_text = (name[0].toString() + name[name.length - 1]).toUpperCase()
        }else if(message.sender_name.contains(' ')){
            val name = message.sender_name.split(" ")
            login_text = (name[0][0].toString() + name[1][0]).toUpperCase()
        }else if(message.sender_name!="" && message.sender_name.length>1){
            login_text = message.sender_name[0].toString() + message.sender_name[1]
        }else if(message.sender_name!=""){
            login_text = message.sender_name[0].toString()
        }

        holder.tv_subject.text = message.subject
        holder.tv_date.text = convert_date(message.date)
        holder.tv_message.text = message.body
        holder.tv_name.text = message.sender_name
        holder.tv_userlogin.text = login_text


        val color_num = Math.abs(message.sender_name.hashCode() + message.sender_name[0].toInt()) % colors.size
        holder.inner_circle.setCardBackgroundColor(colors[color_num])
    }
}
