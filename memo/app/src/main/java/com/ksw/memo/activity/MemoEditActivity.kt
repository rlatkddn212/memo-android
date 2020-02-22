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

import android.content.Intent
import android.database.DatabaseErrorHandler
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.ksw.memo.MemoData
import com.ksw.memo.R
import com.ksw.memo.adapter.ImageEditRecyclerViewAdapter
import com.ksw.memo.db.MemoSQLHelper
import com.ksw.memo.listener.RecyclerItemClickListener
import kotlinx.android.synthetic.main.activity_memo_edit.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_memo_edit.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*


//-------------------------------------------------------------------------------------------------- MemoEditActivity
class MemoEditActivity : AppCompatActivity(), RecyclerItemClickListener.OnRecyclerClickListener{
    private val TAG = "MemoEditActivity"
    val dbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })
    var bNewEdit : Boolean = true
    lateinit var memo : MemoData
    lateinit var imageFile : File
    var imageList :MutableList<String> = ArrayList()
    lateinit var imageAdapter : ImageEditRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // setup intent data
        memo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData
        if (memo.memoId != -1L) {
            bNewEdit = false
        }

        title_edit.setText(memo.title)
        contents_edit.setText(memo.contents)

        // 리사이클러 뷰 연결
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addOnItemTouchListener(
            RecyclerItemClickListener(this, recycler_view, this)
        )

        imageAdapter = ImageEditRecyclerViewAdapter(imageList)
        recycler_view.adapter = imageAdapter

        for (image in memo.imageURL!!) {
            imageList.add(image)
        }

        imageAdapter.notifyDataSetChanged()

        // url 버튼, url 입력 창 생성, 확인 취소 버튼 생성, imageURL List에 추가
        url_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("URL 이미지 불러오기")
            builder.setMessage("URL을 입력하세요.")

            val editText = EditText(this)
            builder.setView(editText)
            builder
                .setPositiveButton("확인") { dialogInterface, i ->
                    memo.imageURL?.add(editText.getText().toString())
                    imageList.add(editText.getText().toString())
                    imageAdapter.notifyDataSetChanged()
                }
                .setNegativeButton("취소") { dialogInterface, i ->

                }
                .show()
        }

        // 카메라 버튼, 저장될 image 위치 지정, 카메라에서 이미지 촬영
        photo_button.setOnClickListener {
            val intent = Intent()
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE)

            val imageFolder = getFilesDir()
            imageFile = File(imageFolder, "memo_" + UUID.randomUUID() + ".jpg")

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                val photoUri = getUriForFile(this, "com.ksw.memo", imageFile)
                Log.d(TAG, "$photoUri")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }else {
                val photoUri: Uri = Uri.fromFile(imageFile)
                Log.d(TAG, "$photoUri")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }

            startActivityForResult(intent, 2);
        }

        // 갤러리에서 이미지 가져오기
        gallery_button.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $requestCode, $resultCode $data")
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // 파일 복사해서 가져온다.
                val imageFolder = getFilesDir()
                imageFile = File(imageFolder, "memo_" + UUID.randomUUID() + ".jpg")

                val inputStream: InputStream? = contentResolver
                    .openInputStream(data?.data!!)
                val fileOutputStream = FileOutputStream(imageFile)
                val buffer = ByteArray(1024)
                var bytesRead = 0

                while (inputStream?.read(buffer).also({  bytesRead = it!! }) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }

                fileOutputStream.close()
                inputStream?.close()

                imageList.add("file://$imageFile")
                imageAdapter.notifyDataSetChanged()
            }
        }
        else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                // 리사이클러 뷰에 추가
                imageList.add("file://$imageFile")
                imageAdapter.notifyDataSetChanged()
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
                finish()
                true
            }
            R.id.add_memo -> {
                memo.imageURL?.clear()
                for (image in imageList) {
                    memo.imageURL?.add(image)
                }

                if (bNewEdit) {
                    dbHelper.addMemo(title_edit?.text.toString(), contents_edit?.text.toString(), memo.imageURL)
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

    override fun onItemClick(view: View, position: Int) {
        // 제거할지?
        val builder = AlertDialog.Builder(this)
        builder.setTitle("이미지 삭제")
        builder.setMessage("이미지를 삭제하시겠습니까?")

        builder
            .setPositiveButton("확인") { dialogInterface, i ->
                memo.imageURL?.removeAt(position)
                imageList.removeAt(position)
                imageAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("취소") { dialogInterface, i ->

            }
            .show()
    }
}
