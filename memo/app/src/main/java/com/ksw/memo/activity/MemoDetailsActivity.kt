/**
 * 메모 상세 보기
 * 메모에 제목, 내용, 사진들을 볼 수 있는 액티비티
 *
 * 요구사항
 * 1.작성된 메모의 제목과 본문을 볼 수 있습니다.
 * 2. 메모에 첨부되어있는 이미지를 볼 수 있습니다. (이미지는 n개 존재 가능)
 * 3. 메뉴를 통해 메모 내용 편집 또는 삭제가 가능합니다.
 */

package com.ksw.memo.activity

import android.content.Intent
import android.database.Cursor
import android.database.DatabaseErrorHandler
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ksw.memo.MemoData
import com.ksw.memo.db.MemoSQLHelper
import com.ksw.memo.R
import com.ksw.memo.adapter.ImageDetailsRecyclerViewAdapter

import kotlinx.android.synthetic.main.activity_memo_details.*
import kotlinx.android.synthetic.main.content_memo_details.*
import java.util.ArrayList

//-------------------------------------------------------------------------------------------------- MemoDetailsActivity
class MemoDetailsActivity : AppCompatActivity() {
    private val TAG = "MemoDetailsActivity"
    val dbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })
    lateinit var memo : MemoData
    val changeCode : Int = 101

    var imageList :MutableList<String> = ArrayList()
    lateinit var imageAdapter : ImageDetailsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_details)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        memo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData

        title_details.text = memo.title
        contents_details.text = memo.contents

        // 리사이클러 뷰 연결
        details_recycler_view.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageDetailsRecyclerViewAdapter(imageList)
        details_recycler_view.adapter = imageAdapter
        updateImageList()

        delete_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(memo.title);
            builder.setMessage("메모를 삭제하시겠습니까?")

            builder
                .setPositiveButton("확인") { dialogInterface, i ->
                    dbHelper.deleteMemo(memo.memoId)
                    val intent = Intent()
                    // intent.putExtra("MEMO_DATA", memo)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .setNegativeButton("취소") { dialogInterface, i ->

                }
                .show()
        }
    }

    private fun updateImageList() {
        val cursor : Cursor = dbHelper.getAllMemoImage(memo.memoId)
        imageList.clear()
        memo.imageURL?.clear()
        if (cursor.moveToFirst()) {
            do {
                imageList.add(
                    cursor.getString(2)
                )
                memo.imageURL?.add( cursor.getString(2))

            } while (cursor.moveToNext())
        }

        imageAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $requestCode, $resultCode")
        if (requestCode == changeCode) {
            if (resultCode == RESULT_OK) {
                memo = data?.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData
                title_details.text = memo.title
                contents_details.text = memo.contents

                updateImageList()
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
            android.R.id.home -> {
                val intent = Intent()
                setResult(RESULT_OK, intent)
                finish()
                true
            }
            R.id.add_memo -> {
                val intent = Intent(this, MemoEditActivity::class.java)
                intent.putExtra("MEMO_DATA", memo)
                startActivityForResult(intent, changeCode)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
