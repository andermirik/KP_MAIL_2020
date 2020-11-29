package com.example.maliclient

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.maliclient.adapter.FolderAdapter
import com.example.maliclient.adapter.MessageAdapter
import com.example.maliclient.adapter.UserAdapter
import com.example.maliclient.model.FolderCard
import com.example.maliclient.model.MessageCard
import com.example.maliclient.model.User
import com.example.maliclient.model.UserCard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.nav_main.*
import kotlinx.android.synthetic.main.nav_main.view.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart


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

    fun on_select_folder(folders : Array<FolderCard>, position: Int){
        folder_name.text = folders[position].fakename
        Toast.makeText(this, folders[position].fakename, Toast.LENGTH_SHORT).show()


        val thread = Thread(Runnable {
            val store = get_IMAP_store(current_user!!)
            val folder = store.getFolder(folders[position].name)
            try {
                folder.open(Folder.READ_ONLY)
                var messages: Array<Message> = arrayOf()
                if(folder.messageCount>11)
                    messages = folder.getMessages(folder.messageCount-10, folder.messageCount)
                else if(folder.messageCount != 0)
                messages = folder.getMessages(1, folder.messageCount)

                val fetchProfile = FetchProfile()
                fetchProfile.add(FetchProfile.Item.ENVELOPE)
                folder.fetch(messages, fetchProfile);

                val messages_cards = cast_messages(messages, folder as UIDFolder)
                val message_adapter = MessageAdapter(this, messages_cards, this)
                runOnUiThread(Runnable{
                    rv_mails.adapter = message_adapter
                })
                folder.close()
            }catch(ex : FolderNotFoundException){
                runOnUiThread(Runnable{
                    Toast.makeText(this, "folder not found", Toast.LENGTH_SHORT).show()
                })
            }
            store.close()
        })
        thread.start()
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

    fun cast_messages(messages:Array<Message>, folder : UIDFolder) : Array<MessageCard>{
        val res = arrayListOf<MessageCard>()
        for(message in messages){
            var sender_name = ""
            var message_subject = ""

            if(message.from != null && message.from.isNotEmpty()){
                val address = (message.from[0] as InternetAddress)
                if(address.personal!=null)
                    sender_name = address.personal
                else
                    sender_name = address.address
            }else{
                sender_name = "me"
            }

            if(message.subject != null)
                message_subject = message.subject

            var message_text = getTextFromMessage(message).trim().replace("\n", "");

            res.add(MessageCard(
                sender_name,
                message_subject,
                message_text,
                message.receivedDate,
                folder.getUID(message)
            ))
        }
        return res.toTypedArray()
    }

    @Throws(MessagingException::class, IOException::class)
    private fun getTextFromMessage(message: Message): String {
        var result = ""
        if (message.isMimeType("text/plain")) {
            result = message.content.toString()
        } else if (message.isMimeType("multipart/*")) {
            val mimeMultipart: MimeMultipart = message.content as MimeMultipart
            result = getTextFromMimeMultipart(mimeMultipart)
        }
        return result
    }

    @Throws(MessagingException::class, IOException::class)
    private fun getTextFromMimeMultipart(
        mimeMultipart: MimeMultipart
    ): String {
        var result = ""
        val count: Int = mimeMultipart.getCount()
        for (i in 0 until count) {
            val bodyPart: BodyPart = mimeMultipart.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                result = """
                    $result
                    ${bodyPart.content}
                    """.trimIndent()
                break // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                val html = bodyPart.content as String
                result = """
                    $result
                    ${Jsoup.parse(html).text()}
                    """.trimIndent()
            } else if (bodyPart.content is MimeMultipart) {
                result = result + getTextFromMimeMultipart(bodyPart.content as MimeMultipart)
            }
        }
        return result
    }

}