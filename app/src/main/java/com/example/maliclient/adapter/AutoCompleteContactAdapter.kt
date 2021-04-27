package com.example.maliclient.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.maliclient.R
import com.example.maliclient.SendMailActivity
import com.example.maliclient.model.MessageDb
import java.lang.Math.min
import kotlin.math.abs


class AutoCompleteContactAdapter(var mContext: Context, var list: ArrayList<MessageDb>)
    : ArrayAdapter<MessageDb>(mContext, R.layout.contact_item, list) {


    var temp_list = arrayListOf<MessageDb>()
    var text = ""
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

    init {
        temp_list.addAll(list)
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var results = FilterResults()
                val suggestions = arrayListOf<MessageDb>()
                if(constraint == null  || constraint.length == 0){
                    suggestions.addAll(list)
                }else{
                    val filterPattern = constraint.toString().toLowerCase().trim()

                    for (contact in temp_list){
                        if(contact.sender_address.toLowerCase().contains(filterPattern)
                            || contact.sender_name.toLowerCase().contains(filterPattern)){
                            suggestions.add(contact)
                        }
                    }
                }
                results.values = suggestions
                results.count = suggestions.size

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                clear()
                addAll(results.values as List<MessageDb>)
                notifyDataSetChanged()
            }

        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        if(view == null)
            view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false)

        if (view != null) {
            val contact : MessageDb = list.get(position)

            var login_text = ""
            if(contact.sender_name.contains('@')){
                val name = contact.sender_name.split("@")[0]
                login_text = (name[0].toString() + name[name.length - 1]).toUpperCase()
            }else if(contact.sender_name.contains(' ')){
                val name = contact.sender_name.split(" ")
                login_text = (name[0][0].toString() + name[1][0]).toUpperCase()
            }else if(contact.sender_name!="" && contact.sender_name.length>1){
                login_text = (contact.sender_name[0].toString().toUpperCase() + contact.sender_name[1])
            }else if(contact.sender_name!=""){
                login_text = contact.sender_name[0].toString().toUpperCase()
            }



            val color_num =
                abs(contact.sender_name.hashCode() + contact.sender_name[0].toInt()) % colors.size
            view.findViewById<CardView>(R.id.inner_circle).setCardBackgroundColor(colors[color_num])
            view.findViewById<TextView>(R.id.tv_userlogin).text = login_text
            view.findViewById<TextView>(R.id.tv_name).text = contact.sender_name
            view.findViewById<TextView>(R.id.tv_address).text = contact.sender_address
            view.setOnClickListener{
                (context as SendMailActivity).onSelectAddress(contact.sender_address)
            }
        }

        return view!!
    }



}