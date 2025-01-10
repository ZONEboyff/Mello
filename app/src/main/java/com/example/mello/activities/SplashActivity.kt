package com.example.mello.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mello.R
import com.example.mello.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val typeFace: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        val tvAppName: TextView = findViewById(R.id.tv_app_name)
        tvAppName.typeface = typeFace
        Handler().postDelayed({
            var currentUserId = FirestoreClass().getCurrentUserId()
            if(currentUserId.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2500)
    }
}