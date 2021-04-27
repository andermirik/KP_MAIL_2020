package com.example.maliclient

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.maliclient.adapter.FolderAdapter
import com.example.maliclient.adapter.MessageAdapter
import com.example.maliclient.adapter.UserAdapter
import com.example.maliclient.model.*
import com.example.maliclient.nav.SwipeHelper
import com.example.maliclient.nav.SwipeHelper.UnderlayButtonClickListener
import com.sun.mail.imap.IMAPFolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.nav_main.*
import kotlinx.android.synthetic.main.nav_main.view.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import javax.mail.*
import javax.mail.internet.ContentType
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(){

    lateinit var dr_header : View
    lateinit var db : AppDatabase

    var current_user : User? = null
    var current_folder = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.TRANSPARENT

        btn_menu.setOnClickListener{
            drawer.openDrawer(Gravity.LEFT)
        }

        btn_search.setOnClickListener{
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
            nav_view.rv_users.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            nav_view.rv_users.adapter = UserAdapter(this, list, this)
            (nav_view.rv_users.adapter as UserAdapter).select(0)
        }

        if(current_user!= null) {
            nav_view.tv_user.text = current_user!!.login
        }else{
            nav_view.tv_user.text = ""
        }
        //cpyilcikjlgmdpqo
        drawer.set(drawer.nav_view, nav_view.rv_users)


        swipe_refresh.setOnRefreshListener {
            load_messages(current_folder)
        }


        val swipeHelper: SwipeHelper = object : SwipeHelper(this, rv_mails) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>
            ) {
                underlayButtons.add(UnderlayButton(
                    this@MainActivity,
                    "Удалить",
                    resources.getDrawable(R.drawable.ic_baseline_delete_sweep_24, null),
                    Color.parseColor("#ff2b2b"),
                    true,
                    UnderlayButtonClickListener {
                        mark_message_delete(it)
                        (rv_mails.adapter as MessageAdapter).removeMessage(it)
                    }
                ))
                underlayButtons.add(UnderlayButton(
                    this@MainActivity,
                    "Прочесть",
                    resources.getDrawable(R.drawable.ic_baseline_mail_outline_24, null),
                    Color.parseColor("#FFFFA726"),
                    false,
                    UnderlayButtonClickListener {
                        mark_message_seen(it)
                        (rv_mails.adapter as MessageAdapter).seenMessage(it)
                    }
                ))
            }
        }

        rv_mails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab.isShown) {
                    fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        fab.setOnClickListener{
            if(current_user!= null) {
                val intent = Intent(this, SendMailActivity::class.java)
                intent.putExtra("user_login", current_user!!.login)
                startActivity(intent)
            }
        }


    }

    fun mark_message_delete(pos : Int){
        thread {
            val message_uid = (rv_mails.adapter as MessageAdapter).messages[pos].message_uid
            val store = get_IMAP_store(current_user!!)
            val folder = store.getFolder(current_folder)

            try {
                folder.open(Folder.READ_WRITE)
                (folder as UIDFolder).getMessageByUID(message_uid).setFlag(Flags.Flag.DELETED, true)
                folder.close(true)
            }catch(e: Exception){
                Log.d("TAG", e.toString())
            }
        }.join()
    }

    fun mark_message_seen(pos : Int){
        thread{
            val message_uid = (rv_mails.adapter as MessageAdapter).messages[pos].message_uid
            val store = get_IMAP_store(current_user!!)
            val folder = store.getFolder(current_folder)

            try {
                folder.open(Folder.READ_WRITE)
                (folder as UIDFolder).getMessageByUID(message_uid).setFlag(Flags.Flag.SEEN, true)
                folder.close(true)
            }catch(e: Exception){}
        }.join()

    }

    fun on_select_message(message_uid: Long){

        thread{
            val store = get_IMAP_store(current_user!!)
            val folder = store.getFolder(current_folder)

            try {
                folder.open(Folder.READ_WRITE)
                (folder as UIDFolder).getMessageByUID(message_uid).setFlag(Flags.Flag.SEEN, true)
                folder.close(true)
            }catch(e: Exception){}
        }

        (rv_mails.adapter as MessageAdapter).seenMessage((rv_mails.adapter as MessageAdapter).getPosByUID(message_uid))

        val intent = Intent(this, MailActivity::class.java)
        intent.putExtra("folder_name", current_folder)
        intent.putExtra("message_uid", message_uid)
        intent.putExtra("username", current_user!!.login)
        startActivity(intent)
    }

    fun on_select_user(users: List<UserCard>, position: Int){
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

    fun load_messages(folder_name: String){
        val thread = Thread(Runnable {
            val store = get_IMAP_store(current_user!!)
            val folder = store.getFolder(folder_name)

            try {
                runOnUiThread(Runnable {
                    swipe_refresh.isRefreshing = true
                })

                folder.open(Folder.READ_ONLY)
                var messages: Array<Message> = arrayOf()

                messages = folder.getMessages(1, folder.messageCount)
                sync_to_db(messages, folder as UIDFolder)
                val messages_db = db.messageDao().getBydFolderNameAndUserName(
                    folder.name,
                    current_user!!.login
                )
                val message_cards = arrayListOf<MessageCard>()

                for (message_db in messages_db)
                    message_cards.add(
                        MessageCard(
                            message_db.sender_name,
                            message_db.sender_address,
                            message_db.subject,
                            message_db.body,
                            message_db.date,
                            message_db.message_uid,
                            message_db.folder_name,
                            message_db.userlogin,
                            message_db.isReaded
                        )
                    )

                //val messages_cards = cast_messages(messages, folder as UIDFolder)
                runOnUiThread(Runnable {
                    message_cards.sortWith(Comparator { message_card, t1 ->
                        val Date: Long = message_card.date.time
                        val Date1: Long = t1.date.time
                        Date.compareTo(Date1)
                    })


                    rv_mails.adapter = MessageAdapter(this, message_cards.toTypedArray(), this)

                    swipe_refresh.isRefreshing = false
                })
                folder.close()
            } catch (ex: FolderNotFoundException) {

                runOnUiThread(Runnable {
                    swipe_refresh.isRefreshing = false
                    Toast.makeText(this, "folder not found", Toast.LENGTH_SHORT).show()
                })
            }

            store.close()
        })
        thread.start()
    }

    fun sync_to_db(messages: Array<Message>, folder: UIDFolder){
        val db_messages = db.messageDao().getBydFolderNameAndUserName(
            current_folder,
            current_user!!.login
        )
        val db_message_uids = arrayListOf<Long>()
        val message_uids = arrayListOf<Long>()

        val fetchProfile = FetchProfile()
        fetchProfile.add(UIDFolder.FetchProfileItem.UID)
        fetchProfile.add(UIDFolder.FetchProfileItem.FLAGS)
        (folder as Folder).fetch(messages, fetchProfile)

        for (db_message in db_messages){
            db_message_uids.add(db_message.message_uid)
        }

        val messages_to_save = arrayListOf<Message>()

        for(message in messages){
            val isReaded = message.flags.contains(Flags.Flag.SEEN)
            val message_uid = folder.getUID(message)

            if(message_uid in db_message_uids){
                val cur_db_message = db_messages.find { messageDb -> messageDb.message_uid == message_uid }
                if(cur_db_message != null && isReaded != cur_db_message.isReaded)
                    db.messageDao().updateIsReadedById(isReaded, message_uid)
                continue
            }

            messages_to_save.add(message)
        }
        val message_cards = cast_messages(messages_to_save.toTypedArray(), folder)

        for(message_card in message_cards) {
            val message_temp = MessageDb(
                message_card.sender_name,
                message_card.sender_address,
                message_card.subject,
                message_card.body,
                message_card.date,
                message_card.message_uid,
                message_card.folder_name,
                message_card.userlogin,
                message_card.isReaded
            )
            db.messageDao().insertAll(message_temp)
        }

        for (message in messages){
           message_uids.add(folder.getUID(message))
        }

        for(db_message in db_messages){
            if(db_message.message_uid !in message_uids)
                db.messageDao().delete(db_message)
        }
    }

    fun on_select_folder(folders: Array<FolderCard>, position: Int){
        folder_name.text = folders[position].fakename
        current_folder = folders[position].name
        load_messages(current_folder)
    }

    fun get_IMAP_store(user: User) : Store{
        val prop_imap = Properties()
        prop_imap.put("mail.store.protocol", "imaps")
        prop_imap["mail.imap.port"] = user.imap_port
        if(user.enable_ssl) {
            prop_imap["mail.imap.ssl.enable"] = "true"
        }
        val store: Store = Session.getInstance(prop_imap).store
        store.connect(user.imap_server, user.login, user.password)
        return store
    }

    fun load_folders(user: User){
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

    fun order_folders(folders: Array<FolderCard>) : Array<FolderCard>{
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

    fun cast_folders(folders: Array<Folder>) : Array<FolderCard>{
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

    fun cast_messages(messages: Array<Message>, folder: UIDFolder) : Array<MessageCard>{

        val fetchProfile = FetchProfile()
        fetchProfile.add(IMAPFolder.FetchProfileItem.ENVELOPE)
        (folder as Folder).fetch(messages, fetchProfile)

        val res = arrayListOf<MessageCard>()
        for(message in messages){
            var sender_name = ""
            var sender_address = ""
            var message_subject = ""
            var message_text = ""

            var isReaded = message.flags.contains(Flags.Flag.SEEN)

            Log.d("TAG", message.messageNumber.toString() + " начато")
            try {
                if (message.from != null && message.from.isNotEmpty()) {
                    val address = (message.from[0] as InternetAddress)
                    sender_address = address.address
                    if (address.personal != null && address.personal.isNotBlank())
                        sender_name = address.personal
                    else
                        sender_name = address.address
                } else {
                    sender_name = "me"
                }
            }catch (e: MessagingException){
                Log.d("TAG", e.toString())
            }

            try {
                if (message.subject != null)
                    message_subject = message.subject
            }catch (e: MessagingException){
                Log.d("TAG", e.toString())
            }
            try{
                message_text = getTextFromMessage(message).trim().replace("\n", "")

            }catch (e: MessagingException){
                Log.d("TAG", e.toString())
            }

            res.add(
                MessageCard(
                    sender_name,
                    sender_address,
                    message_subject,
                    message_text,
                    message.receivedDate,
                    folder.getUID(message),
                    (folder as Folder).name,
                    current_user!!.login,
                    isReaded
                )
            )
        }
        return res.toTypedArray()
    }

    @Throws(IOException::class, MessagingException::class)
    private fun getTextFromMessage(message: Message): String {
        var result: String = ""
        if (message.isMimeType("text/plain")) {
            result = message.content.toString()
        }
        else if (message.isMimeType("multipart/*")) {
            val mimeMultipart =
                message.content as MimeMultipart
            result = getTextFromMimeMultipart(mimeMultipart)
        }
        else if(message.isMimeType("text/html")){
            result = Jsoup.parse(message.content.toString()).text()
        }
        return result
    }

    @Throws(IOException::class, MessagingException::class)
    private fun getTextFromMimeMultipart(
        mimeMultipart: MimeMultipart
    ): String {
        val count = mimeMultipart.count
        if (count == 0) throw MessagingException("Multipart with no body parts not supported.")

        val multipartRelated = ContentType(mimeMultipart.contentType).match("multipart/related")


        if(multipartRelated){
            val part = mimeMultipart.getBodyPart(0)
            val multipartAlt = ContentType(part.contentType).match("multipart/alternative")
            if(multipartAlt) {
                return getTextFromMimeMultipart(part.content as MimeMultipart)
            }
        }else{
            val multipartAlt = ContentType(mimeMultipart.contentType).match("multipart/alternative")
            if (multipartAlt) {
                for (i in 0 until count) {
                    val part = mimeMultipart.getBodyPart(i)
                    if (part.isMimeType("text/html")) {
                        return getTextFromBodyPart(part)
                    }
                }
            }
        }


        var result: String = ""
        for (i in 0 until count) {
            val bodyPart = mimeMultipart.getBodyPart(i)
            result += getTextFromBodyPart(bodyPart)
        }
        return result
    }

    @Throws(IOException::class, MessagingException::class)
    private fun getTextFromBodyPart(
        bodyPart: BodyPart
    ): String {
        var result: String = ""
        if (bodyPart.isMimeType("text/plain") && bodyPart.fileName.isNullOrBlank()) {
            result = bodyPart.content as String
        } else if (bodyPart.isMimeType("text/html")) {
            val html = bodyPart.content as String
            result = Jsoup.parse(html).text()
        } else if (bodyPart.content is MimeMultipart) {
            result =
                getTextFromMimeMultipart(bodyPart.content as MimeMultipart)
        }
        return result
    }
}