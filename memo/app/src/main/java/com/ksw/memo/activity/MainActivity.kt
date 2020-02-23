/**
 * 메모리스트
 * 메모의 간략한 내용을 리스트(리사이클러 뷰)로 표시하는 액티비티
 *
 * 요구사항
 * 1. 로컬 영역에 저장된 메모를 읽어 리스트 형태로 화면에 표시합니다.
 * 2. 리스트에는 메모에 첨부되어있는 이미지의 썸네일, 제목, 글의 일부가 보여집니다.
 * (이미지가 n개일 경우, 첫 번째 이미지가 썸네일이 되어야 함)
 * 3. 리스트의 메모를 선택하면 메모 상세 보기 화면으로 이동합니다.
 * 4. 새 메모 작성하기 기능을 통해 메모 작성 화면으로 이동할 수 있습니다.
 */

package com.ksw.memo.activity

import android.content.Intent
import android.database.Cursor
import android.database.DatabaseErrorHandler
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ksw.memo.MemoData
import com.ksw.memo.R
import com.ksw.memo.adapter.MemoRecyclerViewAdapter
import com.ksw.memo.db.MemoSQLHelper
import com.ksw.memo.listener.RecyclerItemClickListener
import kotlinx.android.synthetic.main.content_main.*

const val INTENT_CODE: Int = 1

//-------------------------------------------------------------------------------------------------- MainActivity
class MainActivity : AppCompatActivity(),
    RecyclerItemClickListener.OnRecyclerClickListener {
    private val TAG = "MainActivity"
    private var _memoList: MutableList<MemoData> = ArrayList()
    private lateinit var _memoAdapter: MemoRecyclerViewAdapter
    private val _dbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")

        // 리사이클러 뷰 연결
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addOnItemTouchListener(
            RecyclerItemClickListener(this, recycler_view, this)
        )

        _memoAdapter = MemoRecyclerViewAdapter(_memoList)
        recycler_view.adapter = _memoAdapter

        updateMemoList()
    }

    /**
     * 메모 리스트 갱신
     */
    private fun updateMemoList() {
        val cursor: Cursor = _dbHelper.getAllMemo()
        _memoList.clear()
        if (cursor.moveToFirst()) {
            do {
                _memoList.add(
                    MemoData(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                    )
                )

            } while (cursor.moveToNext())
        }

        _memoAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $requestCode, $resultCode")
        if (requestCode == INTENT_CODE) {
            if (resultCode == RESULT_OK) {
                updateMemoList()
            }
        }
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
                var memo: MemoData =
                    MemoData()
                intent.putExtra("MEMO_DATA", memo)
                startActivityForResult(intent, INTENT_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 리사이클러 뷰에 리스트 아이템을 클릭
     */
    override fun onItemClick(view: View, position: Int) {
        Log.d(TAG, "onItemClick : $position")
        val memo = _memoAdapter.getMemo(position)
        if (memo != null) {
            val intent = Intent(this, MemoDetailsActivity::class.java)
            intent.putExtra("MEMO_DATA", memo)
            startActivityForResult(intent, INTENT_CODE)
        }
    }
}
