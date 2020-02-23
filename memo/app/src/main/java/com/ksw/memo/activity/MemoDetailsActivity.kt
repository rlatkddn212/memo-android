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

//-------------------------------------------------------------------------------------------------- MemoDetailsActivity
class MemoDetailsActivity : AppCompatActivity() {
    private val TAG = "MemoDetailsActivity"
    private val mDbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })
    private lateinit var mMemo: MemoData
    private val ChangeCode: Int = 101

    private var mImageList:MutableList<String> = ArrayList()
    private lateinit var mImageAdapter: ImageDetailsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_details)
        // setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mMemo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData

        title_details.text = mMemo.title
        contents_details.text = mMemo.contents

        // 리사이클러 뷰 연결
        details_recycler_view.layoutManager = LinearLayoutManager(this)
        mImageAdapter = ImageDetailsRecyclerViewAdapter(mImageList)
        details_recycler_view.adapter = mImageAdapter
        updateImageList()

        delete_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(mMemo.title);
            builder.setMessage("메모를 삭제하시겠습니까?")

            builder
                .setPositiveButton("확인") { dialogInterface, i ->
                    // 파일 삭제
                    for(removeImage in mImageList) {
                        if (removeImage.startsWith("http", 0)) continue
                        val deleteFile = File(Uri.parse(removeImage).path)
                        if (deleteFile.exists()) {
                            Log.d(TAG, "delete file $deleteFile")
                            deleteFile.delete()
                        }
                    }

                    mDbHelper.deleteMemo(mMemo.memoId)
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
        val cursor : Cursor = mDbHelper.getAllMemoImage(mMemo.memoId)
        mImageList.clear()
        mMemo.imageURL?.clear()
        if (cursor.moveToFirst()) {
            do {
                mImageList.add(
                    cursor.getString(2)
                )
                mMemo.imageURL?.add( cursor.getString(2))

            } while (cursor.moveToNext())
        }

        mImageAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $requestCode, $resultCode")
        if (requestCode == ChangeCode) {
            if (resultCode == RESULT_OK) {
                mMemo = data?.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData
                title_details.text = mMemo.title
                contents_details.text = mMemo.contents

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
                intent.putExtra("MEMO_DATA", mMemo)
                startActivityForResult(intent, ChangeCode)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
