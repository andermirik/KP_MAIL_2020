package com.example.maliclient

import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.util.Base64
import android.util.Xml
import android.view.View
import android.webkit.WebSettings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.maliclient.adapter.AttachmentAdapter
import com.example.maliclient.model.AttachmentCard
import com.example.maliclient.model.User
import kotlinx.android.synthetic.main.activity_mail.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.mail.*
import javax.mail.internet.*
import javax.mail.util.ByteArrayDataSource
import kotlin.collections.ArrayList


class MailActivity : AppCompatActivity() {

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

    lateinit var attachments : List<BodyPart>

    lateinit var db : AppDatabase
    lateinit var user: User
    var sender_name = ""
    var iv = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail)

        val folder_name = intent.getStringExtra("folder_name")
        val message_uid = intent.getLongExtra("message_uid", 0)
        val username = intent.getStringExtra("username")

        btn_back.setOnClickListener{
            finish()
        }

        //webview.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        //webview.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webview.settings.useWideViewPort = false
        webview.settings.loadWithOverviewMode = false
        webview.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        webview.settings.supportZoom()
        webview.settings.builtInZoomControls = true
        webview.settings.displayZoomControls = false
        //webview.settings.domStorageEnabled = true
       // webview.settings.setAppCacheEnabled(true)
        //webview.settings.loadsImagesAutomatically = true
        webview.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webview.settings.blockNetworkImage = false
        webview.isScrollbarFadingEnabled=false
        webview.isHorizontalScrollBarEnabled=false
        webview.isVerticalScrollBarEnabled=false
        webview.settings.defaultFontSize = 28
        webview.settings.minimumFontSize=28
        webview.settings.minimumLogicalFontSize=28

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database"
        )
            .allowMainThreadQueries()
            .build()

        user = db.userDao().getByLogin(username!!)[0]
        var sender_mail = ""
        val thread = Thread(Runnable {
            val store = get_IMAP_store(user)
            val folder = store.getFolder(folder_name)
            folder.open(Folder.READ_ONLY)
            val message = (folder as UIDFolder).getMessageByUID(message_uid)


            var message_subject = ""
            val date: Date = message.receivedDate

            if(message.from != null && message.from.isNotEmpty()){
                val address = (message.from[0] as InternetAddress)
                sender_mail = address.address
                if(address.personal!=null && address.personal.isNotBlank())
                    sender_name = address.personal
                else
                    sender_name = address.address
            }else{
                sender_name = "me"
            }

            var login_text = ""
            if(sender_name.contains('@')){
                val name = sender_name.split("@")[0]
                login_text = (name[0].toString() + name[name.length - 1]).toUpperCase()
            }else if(sender_name.contains(' ')){
                val name = sender_name.split(" ")
                login_text = (name[0][0].toString() + name[1][0]).toUpperCase()
            }else if(sender_name!="" && sender_name.length>1){
                login_text = (sender_name[0].toString().toUpperCase() + sender_name[1])
            }else if(sender_name!=""){
                login_text = sender_name[0].toString().toUpperCase()
            }

            if(message.subject != null)
                message_subject = message.subject

            var color_num = 0
            if(sender_name.isNotBlank())
                color_num = Math.abs(sender_name.hashCode() + sender_name[0].toInt()) % colors.size

            runOnUiThread{
                findViewById<TextView>(R.id.tv_subject).text = message.subject
                tv_from.text = sender_name
                tv_userlogin.text = login_text
                tv_time.text = cast_date(date)

                user_card.setCardBackgroundColor(colors[color_num])
            }
            attachments = getAttachments(message)
            var html: String = getTextFromMessage(message)

            html = decrypt(html)

            runOnUiThread{
                val base64version: String =
                    Base64.encodeToString(html.toByteArray(), Base64.DEFAULT)
                webview.loadData(base64version, "text/html; charset=UTF-8", "base64")
                //webview.loadData(html, "text/html; charset=utf-8", "UTF-8")
            }



            check_sign(html)


            runOnUiThread{
                rv_attachments.adapter = AttachmentAdapter(this, attachments.toTypedArray(), folder, sender_mail, user.login, iv)
            }

            //folder.close()
            return@Runnable

        })
        thread.start()

    }

    fun decrypt(text_html: String) : String {
        var aes_key = byteArrayOf()
        for (attachment in attachments) {
            if (attachment.fileName == "aes-${sender_name}.key")
                aes_key = Base64.decode(attachment.inputStream.readBytes().toString(UTF_8).replace("\r\n", ""), 0)
        }

        if(aes_key.isNotEmpty()){
            val buf = db.userkeysDao().getByLogin(user.login, user.login)[0].private_key
            val private_spec = PKCS8EncodedKeySpec(buf)
            val kf: KeyFactory = KeyFactory.getInstance("RSA")
            val private = kf.generatePrivate(private_spec)

            val RSA =
                Cipher.getInstance("RSA/ECB/NoPadding")
            RSA.init(Cipher.DECRYPT_MODE, private)
            val key = Arrays.copyOfRange(RSA.doFinal(aes_key), 112, 128).toString(Charsets.UTF_8)
            val text = text_html.replace("\r\n", "")
            iv = text.slice(0..15)
            val decrypted = decrypt(key, iv, text.slice(16 until text.length))
            return decrypted
        }else{
            return text_html
        }
    }
    //for(attachment in attachments){
        //if(attachment.fileName == "aes-${sender_name}.key")
        //    continue
        //if(attachment.fileName == "sign-public.key")
        //    continue
        //if(attachment.fileName == "sign.sign")
        //   continue

        //val data = attachment.inputStream.readBytes().toString(UTF_8)
    //}

    //attachments.add(aes_key_attachment)


    fun encrypt(
        key: String,
        initVector: String,
        value: String
    ): String {
        try {
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS5PADDING")
            val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
            val skeySpec =
                SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(value.toByteArray())
            return String(Base64.encode(encrypted, 0))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    fun decrypt(
        key: String,
        initVector: String,
        encrypted: String
    ): String {
        try {
            val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
            val skeySpec =
                SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val original =
                cipher.doFinal(Base64.decode(encrypted, 0))
            return String(original)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    fun check_sign(text_html: String){
        try{
            var sign_attachment: BodyPart? = null
            var key_attachment: BodyPart? = null

            for(attachment in attachments){
                if(attachment.fileName=="sign.sign")
                    sign_attachment = attachment
                else if(attachment.fileName=="sign-public.key")
                    key_attachment=attachment
            }

            if(sign_attachment!= null && key_attachment!= null){
                val buf = ByteArray(key_attachment.size)
                key_attachment.inputStream.read(buf, 0, key_attachment.size)

                val X509publicKey = X509EncodedKeySpec(buf)
                val kf: KeyFactory = KeyFactory.getInstance("RSA")
                val pub = kf.generatePublic(X509publicKey)

                val rsa: Signature = Signature.getInstance("MD5withRSA")
                rsa.initVerify(pub)
                val data = text_html.replace("\r\n", "\n").toByteArray(Charsets.UTF_8)
                rsa.update(data)

                val sign_buf = ByteArray(key_attachment.size)
                sign_attachment.inputStream.read(sign_buf, 0, key_attachment.size)

                val verifies: Boolean = rsa.verify(Base64.decode(sign_buf, 0))

                runOnUiThread{
                    if(verifies){
                        tv_signed.visibility = View.VISIBLE
                    }else{
                        tv_unsigned.visibility = View.INVISIBLE
                    }
                }
            }
        }catch(e: Exception){

        }
        return
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try{
            (rv_attachments.adapter as AttachmentAdapter).on_result(requestCode, resultCode, data!!)
        }catch (e: Exception){

        }
    }

    fun cast_attachments(attachments: List<BodyPart>) : Array<AttachmentCard>{
        val attachment_cards = arrayListOf<AttachmentCard>()
        for(attachment in attachments){
            attachment_cards.add(
                AttachmentCard(
                    MimeUtility.decodeText(attachment.fileName),
                    attachment.size,
                    attachment.inputStream
                )
            )
        }
        return  attachment_cards.toTypedArray()
    }

    fun get_IMAP_store(user: User) : Store {
        val prop_imap = Properties()
        prop_imap.put("mail.store.protocol", "imaps")
        if(user.enable_ssl) {
            prop_imap["mail.imap.ssl.enable"] = "true"
        }
        val store: Store = Session.getInstance(prop_imap).store
        store.connect(user.imap_server, user.login, user.password)
        return store
    }

    @Throws(Exception::class)
    fun getAttachments(message: Message): List<BodyPart>{
        val content = message.content
        if (content is String) return ArrayList<BodyPart>()
        if (content is Multipart) {
            val result: MutableList<BodyPart> = ArrayList<BodyPart>()
            for (i in 0 until content.count) {
                result.addAll(getAttachments(content.getBodyPart(i)))
            }
            return result
        }
        return ArrayList<BodyPart>()
    }

    @Throws(Exception::class)
    private fun getAttachments(part: BodyPart): List<BodyPart> {
        val result: MutableList<BodyPart> = ArrayList<BodyPart>()
        val t = part.disposition

        if (Part.ATTACHMENT == part.disposition && !part.fileName.isNullOrBlank()){
            result.add(part)
        }

        val content = part.content
        if (content is Multipart) {
            for (i in 0 until (content ).count) {
                val bodyPart = content.getBodyPart(i)
                result.addAll(getAttachments(bodyPart)!!)
            }
        }
        return result
    }


    @Throws(IOException::class, MessagingException::class)
    private fun getTextFromMessage(message: Message): String {
        var result: String = ""
        if (message.isMimeType("text/plain")) {
            result = message.content.toString()
            runOnUiThread{
                webview.settings.minimumLogicalFontSize=28
            }
        }
        else if (message.isMimeType("multipart/*")) {
            val mimeMultipart =
                message.content as MimeMultipart
            result = getTextFromMimeMultipart(mimeMultipart)
        }
        else if(message.isMimeType("text/html")){
            result = message.content.toString()
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

        runOnUiThread{
            webview.settings.minimumLogicalFontSize=28
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
        if (bodyPart.isMimeType("text/plain")  && bodyPart.fileName.isNullOrBlank()) {
            runOnUiThread{
                webview.settings.minimumLogicalFontSize=28
            }
            result = bodyPart.content as String
        } else if (bodyPart.isMimeType("text/html")) {
            val html = bodyPart.content as String
            result = html
        } else if (bodyPart.content is MimeMultipart) {
            result =
                getTextFromMimeMultipart(bodyPart.content as MimeMultipart)
        }
        return result
    }

    fun cast_date(date:Date): String{
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

        val sday = if(day > 9) day.toString() else "0${day}"


        val shours = if(hours > 9) hours.toString() else "0${hours}"
        val sminutes = if(minutes > 9) minutes.toString() else "0${minutes}"

        val today = Calendar.getInstance()
        today.timeZone = TimeZone.getTimeZone("Europe/Moscow")

        val is_today = year == today[Calendar.YEAR] && month == today[Calendar.MONTH] && day == today[Calendar.DAY_OF_MONTH]
        val is_yesterday = year == today[Calendar.YEAR] && month == today[Calendar.MONTH] && day == today[Calendar.DAY_OF_MONTH]-1

        if(is_today)
            return "Сегодня, ${shours}:${sminutes}"

        if(is_yesterday)
            return "Вчера, ${shours}:${sminutes}"

        if(year == today[Calendar.YEAR]){
            return "${sday} ${smonth}, ${shours}:${sminutes}"
        }
        else{
            return "${sday} ${smonth} ${year}г., ${shours}:${sminutes}"
        }
    }

}