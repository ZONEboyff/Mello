package com.example.mello.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mello.R
import com.example.mello.models.Board
import com.example.mello.utils.Constants

class MembersActivity : AppCompatActivity() {
    private lateinit var mBoardDetails: Board
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_members)
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        setupActionBar()
    }
    fun setupActionBar(){
        val toolBarMainActivity = findViewById<Toolbar>(R.id.toolbar_members_activity)
        setSupportActionBar(toolBarMainActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = resources.getString(R.string.members)
        toolBarMainActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }
}