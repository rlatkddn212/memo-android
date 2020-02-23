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
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_memo_edit.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

const val GALLERY_IMAGE_CODE = 1
const val PHOTO_IMAGE_CODE = 2
//-------------------------------------------------------------------------------------------------- MemoEditActivity
class MemoEditActivity : AppCompatActivity(), RecyclerItemClickListener.OnRecyclerClickListener {
    private val TAG = "MemoEditActivity"
    private val mDbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })
    private var mIsNewEdit: Boolean = true
    lateinit var mMemo: MemoData
    private lateinit var mImageFile: File
    private var mImageList: MutableList<String> = ArrayList()
    private var mRemoveList: MutableList<String> = ArrayList()
    private lateinit var mImageAdapter: ImageEditRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 인텐트로 전달 받은 데이터 적용
        mMemo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData
        if (mMemo.memoId != -1L) {
            mIsNewEdit = false
        }

        title_edit.setText(mMemo.title)
        contents_edit.setText(mMemo.contents)


        // 리사이클러 뷰 연결
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addOnItemTouchListener(
            RecyclerItemClickListener(this, recycler_view, this)
        )

        mImageAdapter = ImageEditRecyclerViewAdapter(mImageList)
        recycler_view.adapter = mImageAdapter

        for (image in mMemo.imageURL!!) {
            mImageList.add(image)
        }

        mImageAdapter.notifyDataSetChanged()

        // url 버튼, url 입력 창 생성, 확인 취소 버튼 생성, imageURL List에 추가
        url_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("URL 이미지 불러오기")
            builder.setMessage("URL을 입력하세요.")

            val editText = EditText(this)
            builder.setView(editText)
            builder
                .setPositiveButton("확인") { dialogInterface, i ->
                    //memo.imageURL?.add(editText.getText().toString())
                    mImageList.add(editText.getText().toString())
                    mImageAdapter.notifyDataSetChanged()
                }
                .setNegativeButton("취소") { dialogInterface, i ->

                }
                .show()
        }

        // 갤러리에서 이미지 가져오기
        gallery_button.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, GALLERY_IMAGE_CODE)
        }

        // 카메라 버튼, 저장될 image 위치 지정, 카메라에서 이미지 촬영
        photo_button.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    mImageFile = createImageFile()
                    mImageFile?.also {
                        val photoUri: Uri = getUriForFile(
                            this,
                            "com.ksw.memo",
                            it
                        )

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        startActivityForResult(takePictureIntent, PHOTO_IMAGE_CODE)
                    }
                }
            }
        }
    }

    /**
     * 이미지 저장 파일 생성
     * @return 저장 파일
     */
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = filesDir
        return File.createTempFile("memo_${timeStamp}_${UUID.randomUUID()}", ".jpg", storageDir)
    }

    /**
     * 인텐트로 카메라, 갤러리 이동 후 결과를 처리
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $requestCode, $resultCode $data")
        if (requestCode == GALLERY_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                // 파일 복사해서 가져온다.
                val imageFolder = filesDir
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                mImageFile = File(imageFolder, "memo_${timeStamp}_${UUID.randomUUID()}.jpg")

                val inputStream: InputStream? = contentResolver
                    .openInputStream(data?.data!!)
                val fileOutputStream = FileOutputStream(mImageFile)
                val buffer = ByteArray(1024)
                var bytesRead = 0

                while (inputStream?.read(buffer).also({ bytesRead = it!! }) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }

                fileOutputStream.close()
                inputStream?.close()

                mImageList.add("file://$mImageFile")
                mImageAdapter.notifyDataSetChanged()
            }
        } else if (requestCode == PHOTO_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                // 리사이클러 뷰에 추가
                mImageList.add("file://$mImageFile")
                mImageAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    /**
     * 뒤로가기(편집 취소), 이미지 편집 완료 처리
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected called")

        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.save_memo -> {
                // 파일 삭제
                for (removeImage in mRemoveList) {
                    val deleteFile = File(Uri.parse(removeImage).path)
                    if (deleteFile.exists()) {
                        Log.d(TAG, "delete file $deleteFile")
                        deleteFile.delete()
                    }
                }

                if (mIsNewEdit) {
                    mDbHelper.addMemo(
                        title_edit?.text.toString(),
                        contents_edit?.text.toString(),
                        mImageList
                    )
                    setResult(RESULT_OK)
                } else {
                    mMemo.title = title_edit?.text.toString()
                    mMemo.contents = contents_edit?.text.toString()
                    mDbHelper.updateMemo(
                        mMemo.memoId,
                        mMemo.title.toString(),
                        mMemo.contents.toString(),
                        mImageList
                    )
                    val intent = Intent()
                    intent.putExtra("MEMO_DATA", mMemo)
                    setResult(RESULT_OK, intent)
                }

                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 메모 수정에 있는 리사이클러 뷰를 선택시 이미지를 제거할지 여부를 묻는 popup을 띄운다.
     */
    override fun onItemClick(view: View, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.image_delete_title_memo_edit)
        builder.setMessage(R.string.image_delete_question_memo_edit)

        builder
            .setPositiveButton("확인") { dialogInterface, i ->
                if (!mImageList[position].startsWith("http", 0)) {
                    mRemoveList.add(mImageList[position])
                }

                mImageList.removeAt(position)
                mImageAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("취소") { dialogInterface, i ->

            }
            .show()
    }
}
