package com.example.mello.activities

import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.content.Intent
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
import com.example.mello.databinding.ActivityMainBinding
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.User
import com.example.mello.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener{
    private var binding :ActivityMainBinding?=null
    private var drawerLayout:DrawerLayout? = null
    var mSelectedImageFileUri:Uri? = null
    private lateinit var mUserName:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        drawerLayout=binding!!.drawerLayout
        setupActionBar()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        FirestoreClass().loadUserData(this)
        val fabCreateBoard:com.google.android.material.floatingactionbutton.FloatingActionButton = findViewById(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra((Constants.NAME), mUserName)
            startActivity(intent)
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

    fun updateNavigationUserDetails(loggedInUser: User) {
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
    }
    companion object{
        const val MY_PROFILE_REQUEST_CODE = 11
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
    }
}