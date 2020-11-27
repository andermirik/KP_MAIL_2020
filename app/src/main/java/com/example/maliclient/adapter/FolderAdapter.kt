package com.example.maliclient.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maliclient.MainActivity
import com.example.maliclient.R
import com.example.maliclient.model.FolderCard

import javax.mail.Folder

class FolderAdapter(var context: Context, var folders: Array<FolderCard>, var main_activity : MainActivity)
    : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    val must_have_folders = arrayListOf(
        "Входящие",
        "Отправленные",
        "Корзина",
        "Спам",
        "Черновики"
    )

    var selected_folder_pos = 0

    fun select(num : Int){
        if (num == RecyclerView.NO_POSITION)
            return

        notifyItemChanged(selected_folder_pos)
        selected_folder_pos = num
        notifyItemChanged(selected_folder_pos)
        notifyDataSetChanged()
        main_activity.on_select_folder(folders, num)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var folder_card = view.findViewById<CardView>(R.id.folder_card)
        var folder_name = view.findViewById<TextView>(R.id.folder_name)

        init{
            folder_card.setOnClickListener{
                select(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return folders.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        if(position == selected_folder_pos){
            holder.folder_card.setCardBackgroundColor(context.resources.getColor(R.color.nav_selected))
        }else{
            holder.folder_card.setCardBackgroundColor(context.resources.getColor(R.color.nav_background))
        }

        val folder = folders[position]
        holder.folder_name.text = folder.fakename
        if(folder.fakename in must_have_folders)
            holder.folder_name.setDrawableColor(R.color.folder_blue)
    }

    fun TextView.setDrawableColor(@ColorRes color: Int) {
        compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(getColor(context, color), PorterDuff.Mode.SRC_IN)
        }
    }

}
