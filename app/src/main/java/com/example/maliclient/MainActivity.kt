package com.example.maliclient

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.maliclient.adapter.FolderAdapter
import com.example.maliclient.adapter.UserAdapter
import com.example.maliclient.model.FolderCard
import com.example.maliclient.model.User
import com.example.maliclient.model.UserCard
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.nav_main.*
import kotlinx.android.synthetic.main.nav_main.view.*
import java.util.*
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    lateinit var dr_header : View
    lateinit var db : AppDatabase

    var current_user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.TRANSPARENT

        btn_menu.setOnClickListener{
            drawer.openDrawer(Gravity.LEFT)
        }

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database"
        )
            .allowMainThreadQueries()
            .build()

        var list = mutableListOf<UserCard>()

        val users = db.userDao().getAll()
        for(user in users){
            list.add(UserCard(user.id, user.login))
        }
        list.add(UserCard(true))

        nav_view.btn_logout.setOnClickListener{
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
        }

        if(db.userDao().getAll().isNotEmpty()) {
            nav_view.rv_users.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            nav_view.rv_users.adapter = UserAdapter(this, list, this)
            (nav_view.rv_users.adapter as UserAdapter).select(0)
        }

        if(current_user!= null) {
            nav_view.tv_user.text = current_user!!.login
        }else{
            nav_view.tv_user.text = ""
        }

        drawer.set(drawer.nav_view, nav_view.rv_users)



        val thread = Thread(Runnable {
            return@Runnable;
//            var message: Message? = null
//            var inbox = store.getFolder("INBOX")
//            inbox.open(Folder.READ_ONLY)
//
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
        nav_view.tv_user.text = users[position].login
        val db_users = db.userDao().getByLogin(users[position].login)
        if(db_users.isNotEmpty()){
            current_user = db_users[0]
            load_folders(current_user!!)
        }

    }

    fun on_add_new_user(){
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun on_select_folder(folder : Array<FolderCard>, position: Int){

    }

    fun get_IMAP_store(user: User) : Store{
        val prop_imap = Properties()
        prop_imap.put("mail.store.protocol", "imaps")
        if(user.enable_ssl) {
            prop_imap["mail.imap.ssl.enable"] = "true"
        }
        val store: Store = Session.getInstance(prop_imap).store
        store.connect(user.imap_server, user.login, user.password)
        return store
    }

    fun load_folders(user : User){
        val thread = Thread(Runnable {
            val store = get_IMAP_store(user)
            val folders = store.defaultFolder.list("*")
            runOnUiThread {
                rv_folders.adapter = FolderAdapter(this, cast_folders(folders), this)
                (rv_folders.adapter as FolderAdapter).select(0)
            }
            store.close()
        })
        thread.start()
    }

    fun order_folders(folders : Array<FolderCard>) : Array<FolderCard>{
        val must_have_folders = arrayListOf(
            "Входящие",
            "Отправленные",
            "Корзина",
            "Спам",
            "Черновики"
        )
        val folder_cards = arrayListOf<FolderCard>()


        for(mh_folder in must_have_folders){
            val folder = folders.find { mh_folder == it.fakename }
            folder?.let { folder_cards.add(it) }
        }

        for(folder in folders){
            if (folder.fakename in must_have_folders || folder.fakename=="")
                continue
            else
                folder_cards.add(folder)
        }

        return folder_cards.toTypedArray()
    }

    fun cast_folders(folders : Array<Folder>) : Array<FolderCard>{
        val folder_map = hashMapOf<String, String>(
            "Drafts" to "Черновики",
            "Outbox" to "Исходящие",
            "Sent" to "Отправленные",
            "Spam" to "Спам",
            "Trash" to "Корзина",
            "SentBox" to "Отправлено",
            "DraftBox" to "Черновики",
            "Social" to "Соц. сети",
            "Newsletters" to "Рассылки",
            "INBOX" to "Входящие",
            "[Gmail]" to ""
        )

        val folder_cards = arrayListOf<FolderCard>()

        for (folder in folders){
            if(folder.name in folder_map.keys){
                folder_cards.add(FolderCard(folder_map[folder.name]!!, folder.name))
            }else{
                folder_cards.add(FolderCard(folder.name, folder.name))
            }
        }

        return order_folders(folder_cards.toTypedArray())
    }

}