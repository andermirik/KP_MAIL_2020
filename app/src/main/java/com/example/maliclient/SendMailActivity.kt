package com.example.maliclient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.commonsware.cwac.anddown.AndDown
import com.example.maliclient.adapter.OutAttachmentAdapter
import com.example.maliclient.model.User
import kotlinx.android.synthetic.main.activity_send_mail.*
import java.security.*
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource
import kotlin.math.sign


class SendMailActivity : AppCompatActivity() {

    val attachments = arrayListOf<BodyPart>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)
        val css = "body{font-family:Helvetica,arial,sans-serif;font-size:14px;line-height:1.6;background-color:#fff;color:#333}body>:first-child{margin-top:0!important}body>:last-child{margin-bottom:0!important}a{color:#4183c4;text-decoration:none}a.absent{color:#c00}a.anchor{display:block;padding-left:30px;margin-left:-30px;cursor:pointer;position:absolute;top:0;left:0;bottom:0}h1,h2,h3,h4,h5,h6{margin:20px 0 10px;padding:0;font-weight:700;-webkit-font-smoothing:antialiased;cursor:text;position:relative}h1:first-child,h1:first-child+h2,h2:first-child,h3:first-child,h4:first-child,h5:first-child,h6:first-child{margin-top:0;padding-top:0}h1:hover a.anchor,h2:hover a.anchor,h3:hover a.anchor,h4:hover a.anchor,h5:hover a.anchor,h6:hover a.anchor{text-decoration:none}h1 code,h1 tt{font-size:inherit}h2 code,h2 tt{font-size:inherit}h3 code,h3 tt{font-size:inherit}h4 code,h4 tt{font-size:inherit}h5 code,h5 tt{font-size:inherit}h6 code,h6 tt{font-size:inherit}h1{font-size:28px;color:#000}h2{font-size:24px;border-bottom:1px solid #ccc;color:#000}h3{font-size:18px}h4{font-size:16px}h5{font-size:14px}h6{color:#777;font-size:14px}blockquote,dl,li,ol,p,pre,table,ul{margin:15px 0}hr{border:0 none;color:#ccc;height:4px;padding:0}body>h2:first-child{margin-top:0;padding-top:0}body>h1:first-child{margin-top:0;padding-top:0}body>h1:first-child+h2{margin-top:0;padding-top:0}body>h3:first-child,body>h4:first-child,body>h5:first-child,body>h6:first-child{margin-top:0;padding-top:0}a:first-child h1,a:first-child h2,a:first-child h3,a:first-child h4,a:first-child h5,a:first-child h6{margin-top:0;padding-top:0}h1 p,h2 p,h3 p,h4 p,h5 p,h6 p{margin-top:0}li p.first{display:inline-block}ol,ul{padding-left:30px}ol :first-child,ul :first-child{margin-top:0}ol :last-child,ul :last-child{margin-bottom:0}dl{padding:0}dl dt{font-size:14px;font-weight:700;font-style:italic;padding:0;margin:15px 0 5px}dl dt:first-child{padding:0}dl dt>:first-child{margin-top:0}dl dt>:last-child{margin-bottom:0}dl dd{margin:0 0 15px;padding:0 15px}dl dd>:first-child{margin-top:0}dl dd>:last-child{margin-bottom:0}blockquote{border-left:4px solid #ddd;padding:0 15px;color:#777}blockquote>:first-child{margin-top:0}blockquote>:last-child{margin-bottom:0}table{padding:0}table tr{border-top:1px solid #ccc;background-color:#fff;margin:0;padding:0}table tr:nth-child(2n){background-color:#f8f8f8}table tr th{font-weight:700;border:1px solid #ccc;text-align:left;margin:0;padding:6px 13px}table tr td{border:1px solid #ccc;text-align:left;margin:0;padding:6px 13px}table tr td :first-child,table tr th :first-child{margin-top:0}table tr td :last-child,table tr th :last-child{margin-bottom:0}img{max-width:100%}span.frame{display:block;overflow:hidden}span.frame>span{border:1px solid #ddd;display:block;float:left;overflow:hidden;margin:13px 0 0;padding:7px;width:auto}span.frame span img{display:block;float:left}span.frame span span{clear:both;color:#333;display:block;padding:5px 0 0}span.align-center{display:block;overflow:hidden;clear:both}span.align-center>span{display:block;overflow:hidden;margin:13px auto 0;text-align:center}span.align-center span img{margin:0 auto;text-align:center}span.align-right{display:block;overflow:hidden;clear:both}span.align-right>span{display:block;overflow:hidden;margin:13px 0 0;text-align:right}span.align-right span img{margin:0;text-align:right}span.float-left{display:block;margin-right:13px;overflow:hidden;float:left}span.float-left span{margin:13px 0 0}span.float-right{display:block;margin-left:13px;overflow:hidden;float:right}span.float-right>span{display:block;overflow:hidden;margin:13px auto 0;text-align:right}code,tt{margin:0 2px;padding:0 5px;white-space:nowrap;border:1px solid #eaeaea;background-color:#f8f8f8;border-radius:3px}pre code{margin:0;padding:0;white-space:pre;border:none;background:0 0}.highlight pre{background-color:#f8f8f8;border:1px solid #ccc;font-size:13px;line-height:19px;overflow:auto;padding:6px 10px;border-radius:3px}pre{background-color:#f8f8f8;border:1px solid #ccc;font-size:13px;line-height:19px;overflow:auto;padding:6px 10px;border-radius:3px}pre code,pre tt{background-color:transparent;border:none}"

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database"
        )
            .allowMainThreadQueries()
            .build()

        val user = db.userDao().getByLogin(intent.getStringExtra("user_login")!!)[0]

        btn_send.setOnClickListener{
            val send_to = edit_to.text.toString()
            val subject = edit_subject.text.toString()

            val text = edit_text.text.toString()

            val html = AndDown().markdownToHtml(text)
            val text_to_send = "<style>$css</style><body>$html</body>"

            val thread = Thread(Runnable {
                sign_data(text_to_send)
                try {
                    val prop_smtp = Properties()
                    prop_smtp["mail.smtp.auth"] = "true"
                    prop_smtp["mail.smtp.port"] = user.smtp_port
                    if (user.enable_ssl) {
                        prop_smtp["mail.smtp.ssl.enable"] = "true"
                    }
                    val session = Session.getInstance(prop_smtp)
                    val transport: Transport = session.transport
                    transport.connect(user.smtp_server, user.login, user.password)

                    val message: MimeMessage = MimeMessage(session)
                    message.setFrom(InternetAddress(user.login))
                    message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(send_to))
                    message.subject = subject

                    val message_body_part : BodyPart = MimeBodyPart()
                    message_body_part.setContent(text_to_send, "text/html")

                    val multipart: Multipart = MimeMultipart()
                    multipart.addBodyPart(message_body_part)

                    for(attachment in attachments){
                        multipart.addBodyPart(attachment)
                    }

                    message.setContent(multipart)

                    //transport.sendMessage(message, InternetAddress.parse(send_to))
                }catch(e: MessagingException){
                    Log.d("TAG", e.toString())
                }
                runOnUiThread{
                    Toast.makeText(this, "сообщение отправлено", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })

            thread.start()


        }

        btn_attach.setOnClickListener{
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Выберите файл для отправки"), 10)
        }

        btn_back.setOnClickListener{
            finish()
        }
    }

    fun sign_data(text: String) : ByteArray{
        val data = text.toByteArray()
        try {
            val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("DSA")
            keyGen.initialize(1024, SecureRandom())
            val pair: KeyPair = keyGen.generateKeyPair()

            val dsa: Signature = Signature.getInstance("MD5/DSA")

            val priv: PrivateKey = pair.getPrivate()
            dsa.initSign(priv)

            dsa.update(data)
            val sig: ByteArray = dsa.sign()
            return sig
        } catch (e: Exception) {
            Log.d("tag", e.toString())
        }
        return byteArrayOf()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 10){
            if(resultCode == Activity.RESULT_OK){
                val uri = data!!.data!!

                val attachment: BodyPart = MimeBodyPart()
                val source: DataSource = ByteArrayDataSource(contentResolver.openInputStream(uri), contentResolver.getType(uri))
                attachment.dataHandler = DataHandler(source)
                attachment.fileName = get_filename_by_uri(uri)

                attachments.add(attachment)
                update_rv()
            }
        }
    }

    fun get_filename_by_uri(uri : Uri) : String{
        contentResolver.query(uri, null, null, null, null).use { cursor ->
            cursor?.let {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                it.moveToFirst()
                return it.getString(nameIndex)
            }
        }
        return ""
    }

    fun update_rv(){
        rv_attachments.adapter = OutAttachmentAdapter(this, attachments.toTypedArray(), this)
    }

    fun on_delete(num: Int){
        attachments.removeAt(num)
        rv_attachments.adapter = OutAttachmentAdapter(this, attachments.toTypedArray(), this)
    }

    fun get_SMTP_transport(user: User) : Transport {
        val prop_smtp = Properties()
        prop_smtp["mail.smtp.auth"] = "true"
        prop_smtp["mail.smtp.port"] = user.smtp_port
        if(user.enable_ssl) {
            prop_smtp["mail.smtp.ssl.enable"] = "true"
        }
        val transport: Transport = Session.getInstance(prop_smtp).transport
        transport.connect(user.smtp_server, user.login, user.password)
        return transport
    }
}