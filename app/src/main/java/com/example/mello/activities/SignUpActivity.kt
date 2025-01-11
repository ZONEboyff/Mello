package com.example.mello.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mello.R
import com.example.mello.databinding.ActivityBaseBinding.inflate
import com.example.mello.databinding.ActivitySignUpBinding
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private var binding : ActivitySignUpBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding!!.root)
        setupActionBar()
    }
    private fun setupActionBar(){
        val toolbarSignUpActivity: Toolbar = findViewById(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbarSignUpActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressed()
        }
        binding!!.btnSignUp.setOnClickListener {
            registerUser()
        }
    }
    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim{it <= ' '}
        val email: String = binding?.etEmail?.text.toString().trim{it <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim{it <= ' '}
        if(validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid,name,registeredEmail)
                        FirestoreClass().registerUser(this,user)
                    }else{
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString())
                    }
                }
        }
    }
    private fun validateForm(name:String,email:String,password:String): Boolean {
        return when {
            name.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name))
                false
            }
            email.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email))
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password))
                false
            }
            else -> {
                true
            }
        }
    }
    fun userRegisteredSuccess(){
        hideProgressDialog()
        Toast.makeText(this,"You are registered successfully.",Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}