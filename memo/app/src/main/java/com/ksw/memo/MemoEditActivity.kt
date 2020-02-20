package com.ksw.memo

import android.database.DatabaseErrorHandler
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_memo_edit.*
import kotlinx.android.synthetic.main.content_memo_edit.*

class MemoEditActivity : AppCompatActivity() {
    private val TAG = "MemoEditActivity"
    val dbHelper = MemoSQLHelper(this, DatabaseErrorHandler{
        Log.e(TAG, "DB Error")
    })

    private var title : EditText? = null
    private var contents : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = titleMemo
        contents = contentsMemo

        val memo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected called")

        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.add_memo -> {
                dbHelper.addMemo(title?.text.toString(), contents?.text.toString(), null)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
