package com.example.maliclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        //firebaseUser = FirebaseAuth.getInstance().currentUser
        //if(firebaseUser != null) {
        intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
        //}else{
        //    intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
        //    startActivity(intent)
        //    finish()
        }

}