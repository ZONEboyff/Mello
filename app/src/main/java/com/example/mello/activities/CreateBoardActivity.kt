package com.example.mello.activities

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mello.R
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.Board
import com.example.mello.utils.Constants
import com.example.mello.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.example.mello.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.example.mello.utils.Constants.getFileExtension
import com.example.mello.utils.Constants.showImageChooser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class CreateBoardActivity : BaseActivity() {
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_board)
        setupActionBar()
        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }
        val ivBoardImage = findViewById<CircleImageView>(R.id.iv_board_image)
        ivBoardImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_IMAGES)==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        findViewById<TextView>(R.id.btn_create).setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }
    fun setupActionBar(){
        val toolBarMainActivity = findViewById<Toolbar>(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolBarMainActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        toolBarMainActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    private fun createBoard(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())
        val board = Board(
            findViewById<TextView>(R.id.et_board_name).text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )
        FirestoreClass().createBoard(this,board)
    }
    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(this,mSelectedImageFileUri)
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL",uri.toString())
                    mBoardImageURL = uri.toString()
                    FirestoreClass().storeImageUrlInFirestore(uri.toString())
                    createBoard()
                }
            }.addOnFailureListener{
                    exception ->
                hideProgressDialog()
                showErrorSnackBar(exception.message.toString())
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            }
        }else{
            showErrorSnackBar("You denied the storage permission for selecting an image.You can allow it from the settings.")
        }
    }
    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_IMAGE_REQUEST_CODE){
                if(data != null){
                    try {
                        mSelectedImageFileUri = data.data!!
                        Glide.with(this@CreateBoardActivity)
                            .load(mSelectedImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.ic_board_place_holder)
                            .into(findViewById(R.id.iv_board_image))
//
                    }catch (e:Exception){
                        showErrorSnackBar(e.message.toString())
                    }
                }
            }
        }
    }
}