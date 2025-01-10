package com.example.mello.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mello.R
import com.example.mello.databinding.ActivitySignInBinding
import com.example.mello.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivitySignInBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding!!.root)
        auth = FirebaseAuth.getInstance()
        binding?.btnSignIn?.setOnClickListener{
            signInRegisteredUser()
        }
        setupActionBar()
    }
    private fun setupActionBar() {
        val toolbar_sign_in_activity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_sign_in_activity)
        setSupportActionBar(toolbar_sign_in_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_in_activity.setNavigationOnClickListener { onBackPressed() }
    }
    private fun signInRegisteredUser() {
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }
        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Toast.makeText(this, "You are logged in successfully.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,MainActivity::class.java))
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString())
                    }
                }
        }
    }
    private fun validateForm(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showErrorSnackBar("Please enter an email address.")
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar("Please enter a password.")
                false
            }
            else -> true
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun signInSuccess(loggedInUser: User){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}