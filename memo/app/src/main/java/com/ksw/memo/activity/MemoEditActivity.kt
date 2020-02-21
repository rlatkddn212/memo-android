/**
 * 메모 편집 및 작성
 * 메모 신규 작성 및 편집 작업을 하는 액티비티
 *
 * 요구사항
 * 1. 제목 입력란과 본문 입력란, 이미지 첨부란이 구분되어 있어야 합니다.
 * (글 중간에 이미지가 들어갈 수 있는 것이 아닌, 첨부된 이미지가 노출되는 부분이 따로 존재)
 * 2. 이미지 첨부란의 ‘추가' 버튼을 통해 이미지 첨부가 가능합니다.
 * 첨부할 이미지는 다음 중 한 가지 방법을 선택해서 추가할 수 있습니다.
 * 이미지는 0개 이상 첨부할 수 있습니다. 외부 이미지의 경우,
 * 이미지를 가져올 수 없는 경우(URL이 잘못되었거나)에 대한 처리도 필요합니다.
 * - 사진첩에 저장되어 있는 이미지
 * - 카메라로 새로 촬영한 이미지
 * - 외부 이미지 주소(URL) (참고: URL로 이미지를 추가하는 경우, 다운로드하여 첨부할 필요는 없습니다.)
 * 3. 편집 시에는 기존에 첨부된 이미지가 나타나며, 이미지를 더 추가하거나 기존 이미지를 삭제할 수 있습니다.
 */

package com.ksw.memo.activity

import android.app.Activity
import android.content.Intent
import android.database.DatabaseErrorHandler
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ksw.memo.MemoData
import com.ksw.memo.db.MemoSQLHelper
import com.ksw.memo.R
import kotlinx.android.synthetic.main.activity_memo_details.*

import kotlinx.android.synthetic.main.activity_memo_edit.*
import kotlinx.android.synthetic.main.activity_memo_edit.toolbar
import kotlinx.android.synthetic.main.content_memo_edit.*

//-------------------------------------------------------------------------------------------------- MemoEditActivity
class MemoEditActivity : AppCompatActivity() {
    private val TAG = "MemoEditActivity"
    val dbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })
    var bNewEdit : Boolean = true
    lateinit var memo : MemoData

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        memo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData

        if (memo.memoId != -1L) {
            bNewEdit = false
        }

        title_edit.setText(memo.title)
        contents_edit.setText(memo.contents)

        url_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("URL 이미지 불러오기")
            builder.setMessage("URL을 입력하세요.")

            val editText = EditText(this)
            builder.setView(editText)
            builder
                .setPositiveButton("확인") { dialogInterface, i ->
                    memo.imageURL?.add(editText.getText().toString())
                }
                .setNegativeButton("취소") { dialogInterface, i ->

                }
                .show()
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
                finish()
                true
            }
            R.id.add_memo -> {
                if (bNewEdit) {
                    dbHelper.addMemo(title_edit?.text.toString(), contents_edit?.text.toString(), null)
                    setResult(RESULT_OK)
                }
                else {
                    memo.title = title_edit?.text.toString()
                    memo.contents = contents_edit?.text.toString()
                    dbHelper.updateMemo(memo.memoId, memo.title.toString(), memo.contents.toString(), memo.imageURL)
                    val intent = Intent()
                    intent.putExtra("MEMO_DATA", memo)
                    setResult(RESULT_OK, intent)
                }

                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
