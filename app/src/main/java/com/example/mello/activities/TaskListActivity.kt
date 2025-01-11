package com.example.mello.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mello.R
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.Board
import com.google.firebase.firestore.DocumentSnapshot

class TaskListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task_list)
        var boardDocumentId = ""
        if (intent.hasExtra("documentId")) {
            boardDocumentId = intent.getStringExtra("documentId")!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDocumentId)
    }

    fun boardDetails(board: Board){
        hideProgressDialog()
        setupActionBar(board.name)
    }
    fun setupActionBar(title:String){
        val toolBarMainActivity = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolBarMainActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = title
        toolBarMainActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }
}