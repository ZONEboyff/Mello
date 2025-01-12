package com.example.mello.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mello.R
import com.example.mello.dialogs.LabelColorListDialog
import com.example.mello.dialogs.MembersListDialog
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.Board
import com.example.mello.models.Card
import com.example.mello.models.SelectedMembers
import com.example.mello.models.Task
import com.example.mello.models.User
import com.example.mello.utils.Constants

class CardDetailsActivity : BaseActivity() {
    private lateinit var mBoardDetails:Board
    private var mTaskListPosition : Int = -1
    private var mCardListPosition : Int = -1
    private var mSelectedColor = ""
    private lateinit var mMembersDetailList:ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setupActionBar()
        val etNameCardDetails = findViewById<EditText>(R.id.et_name_card_details)
        etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        etNameCardDetails.setSelection(etNameCardDetails.text.toString().length)
        val btnUpdateCardDetails = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_update_card_details)
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }
        btnUpdateCardDetails.setOnClickListener {
            if(etNameCardDetails.text.toString().isNotEmpty()) {
                updateCardDetails()
            }else{
                showErrorSnackBar("Please enter a card name.")
            }
        }
        val tvSelectLabelColor = findViewById<TextView>(R.id.tv_select_label_color)
        tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }
        val tvSelectMembers = findViewById<TextView>(R.id.tv_select_members)
        tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        setupSelectedMembersList()
    }
    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }
    fun setupActionBar(){
        val toolBarMainActivity = findViewById<Toolbar>(R.id.toolbar_card_details_activity)
        setSupportActionBar(toolBarMainActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        toolBarMainActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    private fun updateCardDetails(){
        val etNameCard = findViewById<EditText>(R.id.et_name_card_details)
        val card = Card(
           etNameCard.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor
        )
        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)
        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    private fun deleteCard(){
        val cardsList:ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardListPosition)
        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)
        taskList[mTaskListPosition].cards = cardsList
        mBoardDetails.taskList = taskList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card ->{
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun alertDialogForDeleteCard(cardName:String){
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $cardName?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton("No"){dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog:androidx.appcompat.app.AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun colorsList():ArrayList<String>{
        val colorsList:ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }
    private fun setColor(){
        val tvSelectLabelColor = findViewById<TextView>(R.id.tv_select_label_color)
        tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }
    private fun labelColorsListDialog(){
        val colorsList:ArrayList<String> = colorsList()
        val labelColorListDialog = object : LabelColorListDialog(this,colorsList,resources.getString(R.string.str_select_label_color),mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        labelColorListDialog.show()
    }
    private fun membersListDialog() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        if (cardAssignedMembersList.size > 0) {
                        for (i in mMembersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }
        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            mMembersDetailList,
            resources.getString(R.string.str_select_members)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(user.id)) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(user.id)
                    }
                } else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(user.id)
                    for (i in mMembersDetailList.indices) {
                        if (mMembersDetailList[i].id == user.id) {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }
    private fun setupSelectedMembersList(){
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()
        for(i in mMembersDetailList.indices){
            for(j in cardAssignedMembersList){
                if(mMembersDetailList[i].id == j){
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }
        if(selectedMembersList.size>0) {
            selectedMembersList.add(SelectedMembers("", ""))
            findViewById<TextView>(R.id.tv_select_members).visibility = android.view.View.GONE
            val rvSelectedMembersList = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_selected_members_list)
            rvSelectedMembersList.visibility = android.view.View.VISIBLE
            rvSelectedMembersList.layoutManager = GridLayoutManager(this, 6)
            val adapter = com.example.mello.adapters.CardMemberListItemsAdapter(this, selectedMembersList,true)
            rvSelectedMembersList.adapter = adapter
            adapter.setOnClickListener(object : com.example.mello.adapters.CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }
            })
        }else{
            findViewById<TextView>(R.id.tv_select_members).visibility = android.view.View.VISIBLE
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_selected_members_list).visibility = android.view.View.GONE
        }
    }
}