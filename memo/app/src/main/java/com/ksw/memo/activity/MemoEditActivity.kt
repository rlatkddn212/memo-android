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
import kotlin.collections.ArrayList

const val GALLERY_IMAGE_CODE = 1
const val PHOTO_IMAGE_CODE = 2
//-------------------------------------------------------------------------------------------------- MemoEditActivity
class MemoEditActivity : AppCompatActivity(), RecyclerItemClickListener.OnRecyclerClickListener {
    private val TAG = "MemoEditActivity"
    private val _dbHelper = MemoSQLHelper(this, DatabaseErrorHandler {
        Log.e(TAG, "DB Error")
    })
    private var _isNewEdit: Boolean = true
    private lateinit var _memo: MemoData
    private lateinit var _imageFile: File
    private var _imageList: MutableList<String> = ArrayList()
    private var _removeList: MutableList<String> = ArrayList()
    private lateinit var _imageAdapter: ImageEditRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 인텐트로 전달 받은 데이터 적용
        _memo = intent.extras?.getParcelable<MemoData>("MEMO_DATA") as MemoData
        if (_memo.memoId != -1L) {
            _isNewEdit = false
        }

        title_edit.setText(_memo.title)
        contents_edit.setText(_memo.contents)


        // 리사이클러 뷰 연결
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addOnItemTouchListener(
            RecyclerItemClickListener(this, recycler_view, this)
        )

        _imageAdapter = ImageEditRecyclerViewAdapter(_imageList)
        recycler_view.adapter = _imageAdapter
        if(_memo.imageURL != null) {
            for (image in _memo.imageURL!!) {
                _imageList.add(image)
            }
        }

        _imageAdapter.notifyDataSetChanged()

        // url 버튼, url 입력 창 생성, 확인 취소 버튼 생성, imageURL List에 추가
        url_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.image_url_title_memo_edit)
            builder.setMessage(R.string.image_url_question_memo_edit)

            val editText = EditText(this)
            builder.setView(editText)
            builder
                .setPositiveButton(R.string.dialog_ok) { dialogInterface, i ->
                    val urlPath = editText.getText().toString()
                    _imageList.add(urlPath)
                    _imageAdapter.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.dialog_cancel) { dialogInterface, i ->

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
                    _imageFile = createImageFile()
                    _imageFile?.also {
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
                _imageFile = createImageFile()

                val galleryFile = data?.data
                copyGalleryFile(galleryFile, _imageFile)
                _imageList.add("file://$_imageFile")
                _imageAdapter.notifyDataSetChanged()
            }
        } else if (requestCode == PHOTO_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                // 리사이클러 뷰에 추가
                _imageList.add("file://$_imageFile")
                _imageAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 갤러리 파일을 com.ksw.memo/files 로 복사
     */
    private fun copyGalleryFile(galleryFile: Uri?, imageFile: File) {
        if (galleryFile != null){
            val inputStream: InputStream? = contentResolver
                .openInputStream(galleryFile)
            val fileOutputStream = FileOutputStream(imageFile)
            val buffer = ByteArray(1024)
            var bytesRead = 0

            if (inputStream != null) {
                while (inputStream.read(buffer).also({ bytesRead = it }) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
            }

            fileOutputStream.close()
            inputStream?.close()
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
                val addList = getAddedJPGFile()
                removeJPGImageFile(addList)
                finish()
                true
            }
            R.id.save_memo -> {
                removeJPGImageFile(_removeList)

                if (_isNewEdit) {
                    _dbHelper.addMemo(
                        title_edit?.text.toString(),
                        contents_edit?.text.toString(),
                        _imageList
                    )
                    setResult(RESULT_OK)
                } else {
                    _memo.title = title_edit?.text.toString()
                    _memo.contents = contents_edit?.text.toString()
                    _dbHelper.updateMemo(
                        _memo.memoId,
                        _memo.title.toString(),
                        _memo.contents.toString(),
                        _imageList
                    )
                    val intent = Intent()
                    intent.putExtra("MEMO_DATA", _memo)
                    setResult(RESULT_OK, intent)
                }

                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 편집중 추가된 이미지를 모두 찾는다.
     */
    private fun getAddedJPGFile(): List<String> {
        var addList :MutableList<String> = ArrayList()
        if(_memo.imageURL != null) {
            for (image in _imageList) {
                if (!_memo.imageURL!!.contains(image)){
                    addList.add(image)
                }
            }
        }

        return addList
    }

    /**
     * removeList에 파일들을 제거한다.
     */
    private fun removeJPGImageFile(removeList : List<String>) {
        for (removeImage in removeList) {
            val deleteFile = File(Uri.parse(removeImage).path)
            if (deleteFile.exists()) {
                Log.d(TAG, "delete file $deleteFile")
                deleteFile.delete()
            }
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
            .setPositiveButton(R.string.dialog_ok) { dialogInterface, i ->
                if (!_imageList[position].startsWith("http", 0)) {
                    _removeList.add(_imageList[position])
                }

                _imageList.removeAt(position)
                _imageAdapter.notifyDataSetChanged()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialogInterface, i ->

            }
            .show()
    }
}
