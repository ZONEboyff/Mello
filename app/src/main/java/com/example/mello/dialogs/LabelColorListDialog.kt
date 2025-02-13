package com.example.mello.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mello.R
import com.example.mello.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String,
    private val mSelectedColor: String
) : Dialog(context) {
    private var adapter: LabelColorListItemsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setupRecyclerView()
    }
    private fun setupRecyclerView() {
        val rvList = findViewById<RecyclerView>(R.id.rvList)
        rvList.layoutManager = LinearLayoutManager(context)
        rvList.setHasFixedSize(true)
        val adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        rvList.adapter = adapter
        adapter.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }
    protected abstract fun onItemSelected( color: String)
}