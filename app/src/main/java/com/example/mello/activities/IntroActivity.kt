package com.example.mello.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.mello.R

class IntroActivity : BaseActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro_acitvity)
        val btn_sign_up: AppCompatButton = findViewById(R.id.btn_sign_up_intro)
        val btn_sign_in: AppCompatButton = findViewById(R.id.btn_sign_in_intro)
        btn_sign_up.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        btn_sign_in.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}