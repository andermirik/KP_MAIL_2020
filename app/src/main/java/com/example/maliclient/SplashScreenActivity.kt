package com.example.maliclient

import android.content.Intent
import android.net.MailTo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.room.Room

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        var db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database"
        )
            .allowMainThreadQueries()
            .build()

        val users= db.userDao().getAll()

        if(users.isNotEmpty() && intent.action != null && intent.action == Intent.ACTION_SENDTO){
            val intent_do = Intent(this@SplashScreenActivity, SendMailActivity::class.java)
            intent_do.putExtra("user_login", users[0].login)
            intent_do.putExtra("receiver", MailTo.parse(intent.data.toString()).to)
            startActivity(intent_do)
            finish()
        }
        else if(users.isNotEmpty()){
            intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}