package com.ksw.memo

import android.content.Intent
import android.database.DatabaseErrorHandler
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), RecyclerMemoItemClickListener.OnRecyclerClickListener{

    private val TAG = "MainActivity"
    private val memoAdapter = MemoRecyclerViewAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addOnItemTouchListener(RecyclerMemoItemClickListener(this, recycler_view, this))
        recycler_view.adapter = memoAdapter

        val dbHelper = MemoSQLHelper(this, DatabaseErrorHandler{
            Log.e(TAG, "DB Error")
        })
        dbHelper.getAllMemo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected called")
        return when (item.itemId) {
            R.id.add_memo -> {
                val intent = Intent(this, MemoEditActivity::class.java)
                var memo : MemoData = MemoData()
                intent.putExtra("MEMO_DATA", memo)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        Log.d(TAG, "onItemClick")
        //val memo = MemoRecyclerViewAdapter.get
    }
}
