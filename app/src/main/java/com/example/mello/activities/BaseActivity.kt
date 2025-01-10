package com.example.mello.activities

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mello.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

open class BaseActivity : AppCompatActivity() {
    private var doubleBacktoExitPressedOnce = false
    private lateinit var mProgressDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text = text
        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()
    }
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }
    fun getCurrentUserID(): String{
        return Firebase.auth.currentUser!!.uid
    }
    fun doubleBackToExit() {
        if (doubleBacktoExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBacktoExitPressedOnce = true
        Toast.makeText(this, resources.getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()
        android.os.Handler().postDelayed({ doubleBacktoExitPressedOnce = false }, 2000)
    }
    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(resources.getColor(R.color.snackbar_error_color))
        snackBar.show()
    }
}