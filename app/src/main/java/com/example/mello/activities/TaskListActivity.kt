package com.example.mello.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import com.example.mello.R
import com.example.mello.firebase.FirestoreClass
import com.example.mello.models.Board
import com.example.mello.models.Card
import com.example.mello.models.Task
import com.example.mello.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
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
        mBoardDetails = board
        hideProgressDialog()
        setupActionBar()
        val addTaskList = Task(R.string.add_list.toString())
        board.taskList.add(addTaskList)
        val rvTaskList = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_task_list)
        rvTaskList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        rvTaskList.setHasFixedSize(true)
        val adapter = com.example.mello.adapters.TaskListItemsAdapter(this, board.taskList)
        rvTaskList.adapter = adapter
    }
    fun setupActionBar(){
        val toolBarMainActivity = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolBarMainActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = mBoardDetails.name
        toolBarMainActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }
    fun createTaskList(taskListName:String){
        val task = Task(taskListName,FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    fun updateTaskList(position:Int,listName:String,model:Task){
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    fun addCardToTaskList(position: Int, cardName: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())
        val card = Card(cardName, FirestoreClass().getCurrentUserId(), cardAssignedUsersList)
        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)
        val task = Task(mBoardDetails.taskList[position].title, mBoardDetails.taskList[position].createdBy, cardList)
        mBoardDetails.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}