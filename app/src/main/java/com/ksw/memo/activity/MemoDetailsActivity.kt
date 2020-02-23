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
import android.net.Uri
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
import java.io.File
import java.util.ArrayList

const val DETAILS_CODE: Int = 1
//-------------------------------------------------------------------------------------------------- MemoDetailsActivity
class MemoDetailsActivity : AppCompatActivity() {
    private val TAG = "MemoDetailsActivity"
    private val _dbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })
    private lateinit var _memo: MemoData
    private var _imageList:MutableList<String> = ArrayList()
    private lateinit var _imageAdapter: ImageDetailsRecyclerViewAdapter

    /**
     * 1. Intent로 넘겨 받은 데이터 설정
     * 2. 리사이클러 뷰 연결
     * 3. 메모 삭제 리스너 등록
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        _memo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData

        title_details.text = _memo.title
        contents_details.text = _memo.contents

        // 리사이클러 뷰 연결
        details_recycler_view.layoutManager = LinearLayoutManager(this)
        _imageAdapter = ImageDetailsRecyclerViewAdapter(_imageList)
        details_recycler_view.adapter = _imageAdapter
        updateImageList()

        delete_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(_memo.title)
            builder.setMessage(R.string.memo_delete_question_memo_detail)

            builder
                .setPositiveButton(R.string.dialog_ok) { dialogInterface, i ->
                    // 파일 삭제
                    for (removeImage in _imageList) {
                        if (removeImage.startsWith("http", 0)) continue
                        val deleteFile = File(Uri.parse(removeImage).path)
                        if (deleteFile.exists()) {
                            deleteFile.delete()
                        }
                    }

                    _dbHelper.deleteMemo(_memo.memoId)
                    val intent = Intent()
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .setNegativeButton(R.string.dialog_cancel) { dialogInterface, i ->

                }
                .show()
        }
    }

    /**
     * 이미지를 갱신
     */
    private fun updateImageList() {
        val cursor : Cursor = _dbHelper.getAllMemoImage(_memo.memoId)
        _imageList.clear()
        _memo.imageURL?.clear()
        if (cursor.moveToFirst()) {
            do {
                _imageList.add(
                    cursor.getString(2)
                )
                _memo.imageURL?.add( cursor.getString(2))

            } while (cursor.moveToNext())
        }

        _imageAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $requestCode, $resultCode")
        if (requestCode == DETAILS_CODE) {
            if (resultCode == RESULT_OK) {
                _memo = data?.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData
                title_details.text = _memo.title
                contents_details.text = _memo.contents
                updateImageList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
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
            R.id.edit_memo -> {
                val intent = Intent(this, MemoEditActivity::class.java)
                intent.putExtra("MEMO_DATA", _memo)
                startActivityForResult(intent, DETAILS_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
