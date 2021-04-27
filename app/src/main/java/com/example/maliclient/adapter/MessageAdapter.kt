package com.example.maliclient.adapter

import android.content.Context
import android.graphics.Color
import android.opengl.Visibility
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
import kotlin.collections.ArrayList

class MessageAdapter(
    var context: Context,
    var messages: Array<MessageCard>,
    var main_activity: MainActivity
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    var months = arrayOf(
        "январь",
        "февраль",
        "март",
        "апрель",
        "май",
        "июнь",
        "июль",
        "август",
        "сентябрь",
        "октябрь",
        "ноябрь",
        "декабрь"
    )

    fun select(num: Int){
        if (num == RecyclerView.NO_POSITION)
            return

        main_activity.on_select_message(messages[num].message_uid)
    }

    init {
        val temp = messages.toMutableList()

        val cyear = Calendar.getInstance().get(Calendar.YEAR)
        val cmonth = Calendar.getInstance().get(Calendar.MONTH)
        val cday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val cweek = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)

        val set = mutableSetOf<String>()

        for (i in 1..messages.size){

            val message = messages[messages.size - i]

            val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
            cal.time = message.date
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            val day = cal[Calendar.DAY_OF_MONTH]
            val week = cal[Calendar.WEEK_OF_MONTH]
            val hours = cal[Calendar.HOUR_OF_DAY]
            val minutes = cal[Calendar.MINUTE]


            var date = ""

            if(day == cday){
                date = "Сегодня"
                if(date !in set)
                    temp.add(messages.size - i + 1, MessageCard(date,true))
                set.add(date)
            }
            else if(year == cyear && week == cweek-1){
                date = "На прошлой неделе"
                if(date !in set)
                    temp.add(messages.size - i + 1, MessageCard(date,true))
                set.add(date)
            }else if (month == cmonth){
                date = "В этом месяце"
                if(date !in set)
                    temp.add(messages.size - i + 1, MessageCard(date,true))
                set.add(date)
            }else if(year == cyear){
                date = months[month]
                if(date !in set)
                    temp.add(messages.size - i + 1, MessageCard(date,true))
                set.add(date)
            }else{
                date = months[month] + " " + year.toString()
                if(date !in set)
                    temp.add(messages.size - i + 1, MessageCard(date,true))
                set.add(date)
            }


        }
        messages = temp.toTypedArray()
    }

    fun removeMessage(pos : Int){
        val temp = messages.toMutableList()
        temp.removeAt(pos)
        messages = temp.toTypedArray()
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, messages.size)
    }

    fun getPosByUID(uid : Long) : Int{
        for (i in messages.indices){
            if(messages[i].message_uid == uid)
                return i
        }
        return -1
    }

    fun seenMessage(pos : Int){
        messages[pos].isReaded = true
        notifyItemChanged(pos)
    }

    inner class ViewHolderMessage(view: View) : RecyclerView.ViewHolder(view) {
        var inner_circle = view.findViewById<CardView>(R.id.inner_circle)
        var circle_is_readed = view.findViewById<CardView>(R.id.circle_is_readed)
        var message_card = view.findViewById<ConstraintLayout>(R.id.message_card)
        var tv_message = view.findViewById<TextView>(R.id.tv_message)
        var tv_date = view.findViewById<TextView>(R.id.tv_date)
        var tv_name = view.findViewById<TextView>(R.id.tv_name)
        var tv_subject = view.findViewById<TextView>(R.id.tv_subject)
        var tv_userlogin = view.findViewById<TextView>(R.id.tv_userlogin)

        init{
            message_card.setOnClickListener{
                select(adapterPosition)
                //Toast.makeText(context, messages[adapterPosition].message_uid.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ViewHolderDate(view: View) : RecyclerView.ViewHolder(view) {
        var tv_date = view.findViewById<TextView>(R.id.tv_date_mail)
    }


    override fun getItemViewType(position: Int): Int {
        if(messages[position].isForDate)
            return 0
        else return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==0){
            val view = LayoutInflater.from(context).inflate(R.layout.item_mail_date, parent, false)
            return ViewHolderDate(view)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.item_mail, parent, false)
            return ViewHolderMessage(view)
        }


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
            0 -> smonth = "янв."
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

        val sday = if(day > 9) day.toString() else "0${day}"


        val shours = if(hours > 9) hours.toString() else "0${hours}"
        val sminutes = if(minutes > 9) minutes.toString() else "0${minutes}"

        val today = Calendar.getInstance()
        today.timeZone = TimeZone.getTimeZone("Europe/Moscow")

        val is_today = year == today[Calendar.YEAR] && month == today[Calendar.MONTH] && day == today[Calendar.DAY_OF_MONTH]
        val is_yesterday = year == today[Calendar.YEAR] && month == today[Calendar.MONTH] && day == today[Calendar.DAY_OF_MONTH]-1

        if(is_today)
            return "${shours}:${sminutes}"

        if(is_yesterday)
            return "Вчера"

        if(year == today[Calendar.YEAR]){
            return "${sday} ${smonth}"
        }
        else{
            return "${sday} ${smonth} ${year}г."
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if(!message.isForDate){
            val holder: ViewHolderMessage = holder as ViewHolderMessage

            var login_text = ""
            if(message.sender_name.contains('@')){
                val name = message.sender_name.split("@")[0]
                login_text = (name[0].toString() + name[name.length - 1]).toUpperCase()
            }else if(message.sender_name.contains(' ')){
                val name = message.sender_name.split(" ")
                login_text = (name[0][0].toString() + name[1][0]).toUpperCase()
            }else if(message.sender_name!="" && message.sender_name.length>1){
                login_text = (message.sender_name[0].toString().toUpperCase() + message.sender_name[1])
            }else if(message.sender_name!=""){
                login_text = message.sender_name[0].toString().toUpperCase()
            }

            holder.tv_subject.text = message.subject
            holder.tv_date.text = convert_date(message.date)
            holder.tv_message.text = message.body
            holder.tv_name.text = message.sender_name
            holder.tv_userlogin.text = login_text

            if(message.sender_name.isNotBlank()) {
                val color_num =
                    Math.abs(message.sender_name.hashCode() + message.sender_name[0].toInt()) % colors.size
                holder.inner_circle.setCardBackgroundColor(colors[color_num])
            }

            if(message.isReaded)
                holder.circle_is_readed.visibility = View.GONE
            else
                holder.circle_is_readed.visibility = View.VISIBLE

        }else{
            val holder: ViewHolderDate = holder as ViewHolderDate
            holder.tv_date.text = message.body
        }
    }





}
