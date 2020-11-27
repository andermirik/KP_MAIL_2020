package com.example.maliclient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.maliclient.model.User
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import java.util.*
import javax.mail.Session
import javax.mail.Store
import javax.mail.Transport


class LoginActivity : AppCompatActivity() {
    lateinit var sheetBehavior : BottomSheetBehavior<LinearLayout>
    lateinit var db : AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottom_sheet.visibility = View.VISIBLE

        login_root.setOnClickListener{
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        image_settings.setOnClickListener{
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        sw_enable_ssl.setOnClickListener{
            edit_imap_port.setText((if(sw_enable_ssl.isChecked) 993 else 143).toString())
            edit_smtp_port.setText((if(sw_enable_ssl.isChecked) 465 else 587).toString())
        }

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database"
        )
            .allowMainThreadQueries()
            .build()


    }



    fun on_chose_param(view : View){
        intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun login_onclick(view: View) {
        val email = edit_email.text.toString().toLowerCase()
        val password = edit_password.text.toString()
        val imap_port = edit_imap_port.text.toString()
        val imap_server = edit_imap_server.text.toString()
        val smtp_port = edit_imap_port.text.toString()
        val smtp_server = edit_imap_server.text.toString()

        val enable_ssl = sw_enable_ssl.isChecked

        if(email=="") {
            Toast.makeText(this@LoginActivity, "please write email", Toast.LENGTH_SHORT).show()
        }
        else if(password==""){
            Toast.makeText(this@LoginActivity, "please write password", Toast.LENGTH_SHORT).show()
        }else{
            val prop_imap = Properties()
            val prop_smtp = Properties()

            prop_imap["mail.store.protocol"] = "imaps"
            prop_smtp["mail.smtp.auth"] = "true"

            if(enable_ssl) {
                prop_imap["mail.imap.ssl.enable"] = "true"
                prop_smtp["mail.smtp.ssl.enable"] = "true"
            }

            var default_imap_server = ""
            var default_imap_port = 0

            var default_smtp_server = ""
            var default_smtp_port = 0

            var imap_server = ""
            var imap_port = 0

            var smtp_server = ""
            var smtp_port = 0

            try{
                default_imap_server = "imap." + email.split('@')[email.split('@').size-1]
                default_imap_port = if(sw_enable_ssl.isChecked) 993 else 143

                default_smtp_server = "smtp." + email.split('@')[email.split('@').size-1]
                default_smtp_port = if(sw_enable_ssl.isChecked) 465 else 587

                imap_server = default_imap_server
                imap_port = default_imap_port

                smtp_server = default_smtp_server
                smtp_port = default_smtp_port

                prop_smtp.put("mail.smtp.port", smtp_port)
                prop_imap.put("mail.imap.port", imap_port)

                if(edit_imap_server.text.toString() != ""){
                    imap_server = edit_imap_server.text.toString()
                }else{
                    edit_imap_server.setText(default_imap_server)
                }

                if(edit_imap_port.text.toString() != ""){
                    imap_port = edit_imap_port.text.toString().toInt()
                }else{
                    edit_imap_port.setText(default_imap_port.toString())
                }

                if(edit_smtp_server.text.toString() != ""){
                    smtp_server = edit_smtp_server.text.toString()
                }else{
                    edit_smtp_server.setText(default_smtp_server)
                }

                if(edit_smtp_port.text.toString() != ""){
                    smtp_port = edit_smtp_port.text.toString().toInt()
                }else{
                    edit_smtp_port.setText(default_smtp_port.toString())
                }

            }catch (e : Exception){

            }

            val thread = Thread(Runnable {
                try{
                    var store: Store = Session.getInstance(prop_imap).store
                    store.connect(imap_server, email, password)
                    store.close()
                }catch(e : Exception){
                    runOnUiThread{
                        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        Toast.makeText(this@LoginActivity, "Cant connect IMAP. Please correct the IMAP connection parameters", Toast.LENGTH_LONG).show()
                        (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).
                        hideSoftInputFromWindow(btn_login.windowToken, 0)
                    }
                    return@Runnable
                }

                try{
                    var transport: Transport = Session.getInstance(prop_smtp).transport
                    transport.connect(smtp_server, email, password)
                    transport.close()
                }catch(e : Exception){
                    runOnUiThread{
                        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        Toast.makeText(this@LoginActivity, "Cant connect SMTP. Please correct the SMTP connection parameters", Toast.LENGTH_LONG).show()
                        (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).
                        hideSoftInputFromWindow(btn_login.windowToken, 0)
                    }
                    return@Runnable
                }

                var a = db.userDao().getByLogin(email)
                if(a.isEmpty()){
                    db.userDao().insertAll(User(email, password, imap_server, imap_port, smtp_server, smtp_port, enable_ssl))
                }

                runOnUiThread {
                    intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                return@Runnable
            })
            thread.start()
        }



    }
}