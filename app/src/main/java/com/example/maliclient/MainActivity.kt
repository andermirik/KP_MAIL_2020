package com.example.maliclient

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maliclient.adapter.UserAdapter
import com.example.maliclient.model.UserCard
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.android.synthetic.main.nav_header.view.tv_user
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store


class MainActivity : AppCompatActivity() {

    lateinit var dr_header : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_menu.setOnClickListener{
            drawer.openDrawer(Gravity.LEFT)
        }

        var list = mutableListOf<UserCard>()
        list.add(UserCard("andermirik@yandex.ua"))
        list.add(UserCard("andermiria@yandex.ua"))
        list.add(UserCard("andermirib@yandex.ua"))
        list.add(UserCard("andermiric@yandex.ua"))
        list.add(UserCard("andermirid@yandex.ua"))
        list.add(UserCard(true))


        dr_header = navigation_view.getHeaderView(0)
        dr_header.tv_user.text = "andermirik@yandex.ua"
        dr_header.rv_users.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dr_header.rv_users.adapter = UserAdapter(this, list, this)
        (dr_header.rv_users.adapter as UserAdapter).select(3)

        drawer.set(drawer.navigation_view, dr_header.rv_users)


        window.setStatusBarColor(Color.TRANSPARENT);

        val thread = Thread(Runnable {
            var message: Message? = null

            var prop = Properties()
            prop.put("mail.store.protocol", "imaps")

            var store: Store = Session.getInstance(prop).store
            store.connect("imap.yandex.ua", "andermirik@yandex.ua", "dpeknsgxqxbhhedy")
            var folders = store.defaultFolder.list()
            return@Runnable;
            //var inbox = store.getFolder("INBOX")
            //inbox.open(Folder.READ_ONLY)

//            inbox.messageCount
//
//            message = inbox.getMessage(inbox.messageCount)
//            var multipart : Multipart = message?.content as Multipart
//            var a = multipart.contentType

            //var body  = multipart.getBodyPart(0).content as Multipart
            //var body2 = body.getBodyPart(1).content
//            inbox.close()
        })

        thread.start()


    }

    fun on_select_user(users : List<UserCard>, position : Int){
        dr_header.tv_user.text = users[position].login
    }

    fun on_add_new_user(){
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}