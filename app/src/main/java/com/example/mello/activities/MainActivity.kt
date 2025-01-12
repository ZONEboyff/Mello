package com.example.mello.activities

import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.mello.R
import com.example.mello.adapters.BoardItemsAdapter
import com.example.mello.databinding.ActivityMainBinding
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.Board
import com.example.mello.models.User
import com.example.mello.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener{
    private var binding :ActivityMainBinding?=null
    private var drawerLayout:DrawerLayout? = null
    var mSelectedImageFileUri:Uri? = null
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        drawerLayout=binding!!.drawerLayout
        setupActionBar()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.MELLO_PREFERENCES, MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if(tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        }else{
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val token = task.result
                    updateFCMToken(token!!)
                }
            }
        }
        FirestoreClass().loadUserData(this,true)
        val fabCreateBoard:com.google.android.material.floatingactionbutton.FloatingActionButton = findViewById(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra((Constants.NAME), mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }
    private fun setupActionBar(){
        val toolBarMainActivity = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolBarMainActivity)
        toolBarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolBarMainActivity.setNavigationOnClickListener{
            toggleDrawer()
        }
    }
    private fun toggleDrawer(){
        if(drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile->{
                startActivityForResult(Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out->{
                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear().apply()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

    fun updateNavigationUserDetails(loggedInUser: User,readBoardsList:Boolean) {
        hideProgressDialog()
        mUserName = loggedInUser.name
        val uriImage = Uri.parse(loggedInUser.image)
        Glide
            .with(this)
            .load(uriImage)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById<NavigationView>(R.id.nav_view).getHeaderView(0).findViewById(R.id.nav_user_image))
        val headerView = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
        val tvUsername = headerView.findViewById<TextView>(R.id.tv_username)
        tvUsername.text = loggedInUser.name
        if(readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }
    companion object{
        const val MY_PROFILE_REQUEST_CODE = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        if(resultCode == RESULT_OK){
            if(requestCode == MY_PROFILE_REQUEST_CODE){
                FirestoreClass().loadUserData(this)
            }
        }
        if(resultCode == RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardsList(this)
        }
    }
    fun populateBoardsListToUI(boardsList:ArrayList<Board>){
        hideProgressDialog()
        val rvBoardsList:androidx.recyclerview.widget.RecyclerView = findViewById(R.id.rv_boards_list)
        if(boardsList.size>0) {
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = android.view.View.GONE
            rvBoardsList.visibility = android.view.View.VISIBLE
            rvBoardsList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)
            val adapter = BoardItemsAdapter(this, boardsList)
            rvBoardsList.adapter = adapter
            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = android.view.View.VISIBLE
            rvBoardsList.visibility = android.view.View.GONE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }
    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}