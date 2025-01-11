package com.example.mello.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mello.R
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.User
import com.example.mello.networks.ImgbbApi
import com.example.mello.utils.Constants
import com.example.mello.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.example.mello.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.example.mello.utils.Constants.getFileExtension
import com.example.mello.utils.Constants.showImageChooser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri:Uri? = null
    private lateinit var mUserDetails:User
    private var mProfileImageURL:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_profile)
        setupActionBar()
        FirestoreClass().loadUserData(this)
        val ivUserImage = findViewById<CircleImageView>(R.id.iv_user_image)
        ivUserImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_IMAGES)==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    READ_STORAGE_PERMISSION_CODE)
            }
        }
        findViewById<TextView>(R.id.btn_update).setOnClickListener {
            if(mSelectedImageFileUri!=null) {
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
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
                        Glide.with(this)
                            .load(mSelectedImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_place_holder)
                            .into(findViewById(R.id.iv_user_image))
//                        mSelectedImageFileUri?.let { uri ->
//                            val imageFile =
//                                getPathFromURI(uri)?.let { File(it) }
//                            val apiKey = Constants.API_KEY
//                            if (imageFile != null) {
//                                uploadImageToImgbb(imageFile, apiKey)
//                            }
//                        }
                    }catch (e:Exception){
                        showErrorSnackBar(e.message.toString())
                    }
                }
            }
        }
    }
    private fun getPathFromURI(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (it.moveToFirst()) {
                path = it.getString(columnIndex)
            }
            it.close()
        }
        return path
    }
    fun setupActionBar(){
        val toolBarMainActivity = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolBarMainActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = resources.getString(R.string.my_profile)
        toolBarMainActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }
    @SuppressLint("SetTextI18n")
    fun setUserDataInUI(user: User){
        mUserDetails = user
        val name = user.name
        val email = user.email
        val mobile = user.mobile
        val image = user.image
        Glide.with(this@MyProfileActivity)
            .load(image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_user_image))
        findViewById<TextView>(R.id.et_name).text = name
        findViewById<TextView>(R.id.et_email).text = email
        if(user.mobile!=0L){
            findViewById<TextView>(R.id.et_mobile).text = mobile.toString()
        }
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null){
            val sRef:StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(this,mSelectedImageFileUri))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL",uri.toString())
                    mProfileImageURL = uri.toString()
                    FirestoreClass().storeImageUrlInFirestore(uri.toString())
                    updateUserProfileData()
                }
            }.addOnFailureListener{
                exception ->
                hideProgressDialog()
                showErrorSnackBar(exception.message.toString())
            }
        }
    }
@OptIn(DelicateCoroutinesApi::class)
fun uploadImageToImgbb(imageFile: File, apiKey: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val imgbbApi = retrofit.create(ImgbbApi::class.java)
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
        val expiration = 600
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = imgbbApi.uploadImage(apiKey, expiration, body)

                if (response.isSuccessful) {
                    val imageUrl = response.body()?.data?.url
                    Log.d("ImgBB", "Image uploaded successfully: $imageUrl")
                    // Store the image URL in Firestore
                    FirestoreClass().storeImageUrlInFirestore(imageUrl)
                } else {
                    Log.e("ImgBB", "Error uploading image: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ImgBB", "Upload failed", e)
            }
        }
    }
    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()
        val name = findViewById<TextView>(R.id.et_name).text.toString().trim{it <= ' '}
        val mobile = findViewById<TextView>(R.id.et_mobile).text.toString().trim{it <= ' '}
        if(name != mUserDetails.name&& name.isNotEmpty()){
            userHashMap[Constants.NAME] = name
        }
        if(mobile.isNotEmpty() && mobile.toLong() != mUserDetails.mobile){
            userHashMap[Constants.MOBILE] = mobile.toLong()
        }
        if(mProfileImageURL.isNotEmpty()&& mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }

}