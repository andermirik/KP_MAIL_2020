package com.example.maliclient

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.maliclient.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import java.lang.Exception
import java.util.*
import javax.mail.Session
import javax.mail.Store

class LoginActivity : AppCompatActivity() {
    lateinit var sheetBehavior : BottomSheetBehavior<LinearLayout>

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
        }

    }



    fun on_chose_param(view : View){
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun login_onclick(view: View) {
        val email = edit_email.text.toString()
        val password = edit_password.text.toString()
        val imap_port = edit_imap_port.text.toString()
        val imap_server = edit_imap_server.text.toString()
        val enable_ssl = sw_enable_ssl.isChecked

        if(email=="") {
            Toast.makeText(this@LoginActivity, "please write email", Toast.LENGTH_SHORT).show()
        }
        else if(password==""){
            Toast.makeText(this@LoginActivity, "please write password", Toast.LENGTH_SHORT).show()
        }else{
            val prop = Properties()
            prop["mail.store.protocol"] = "imaps"

            if(enable_ssl)
                prop["mail.imap.ssl.enable"] = "true"


            try{
                var default_imap_server = "imap" + email.split('@')[email.split('@').size-1]
                var default_imap_port = if(sw_enable_ssl.isChecked) 993 else 143

                var imap_server = default_imap_server
                var imap_port = default_imap_port

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
            }catch (e : Exception){

            }

            try{
                val thread = Thread(Runnable {
                    try{
                    var store: Store = Session.getInstance(prop).store

                    store.connect(imap_server, email, password)

                    intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                    }catch(e : Exception){
                        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        Toast.makeText(this@LoginActivity, "Please correct the connection parameters", Toast.LENGTH_LONG).show()
                        (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).
                        hideSoftInputFromWindow(btn_login.windowToken, 0)
                    }
                    return@Runnable
                })
                thread.start()
                thread.join()

            }catch(e : Exception){
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                Toast.makeText(this@LoginActivity, "Please correct the connection parameters", Toast.LENGTH_LONG).show()
                (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).
                        hideSoftInputFromWindow(btn_login.windowToken, 0)
            }


        }



    }
}