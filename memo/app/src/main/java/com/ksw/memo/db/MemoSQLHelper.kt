package com.ksw.memo.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

//-------------------------------------------------------------------------------------------------- MemoStorage
object MemoStorage {
    object MemoTable : BaseColumns {
        const val TABLE_NAME = "memo"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_CONTENTS = "contents"
        const val COLUMN_NAME_THUMBNAIL = "thumbnail"
    }

    object MemoImageTable : BaseColumns {
        const val TABLE_NAME = "memoimage"
        const val COLUMN_NAME_MEMO_ID = "memoid"
        const val COLUMN_NAME_IMAGE_URL= "imageURL"
    }

    const val SQL_CREATE_MEMO =
        "CREATE TABLE ${MemoTable.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${MemoTable.COLUMN_NAME_TITLE} TEXT," +
                "${MemoTable.COLUMN_NAME_CONTENTS} TEXT," +
                "${MemoTable.COLUMN_NAME_THUMBNAIL} TEXT)"

    const val SQL_DELETE_MEMO = "DROP TABLE IF EXISTS ${MemoTable.TABLE_NAME}"

    const val SQL_CREATE_MEMO_IMAGE =
        "CREATE TABLE ${MemoImageTable.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${MemoImageTable.COLUMN_NAME_MEMO_ID} INTEGER," +
                "${MemoImageTable.COLUMN_NAME_IMAGE_URL} TEXT)"

    const val SQL_DELETE_MEMO_IMAGE = "DROP TABLE IF EXISTS ${MemoImageTable.TABLE_NAME}"
}

//-------------------------------------------------------------------------------------------------- MemoSQLHelper
/**
 *  Memo 를 SQLite DB에 저장한다.
 */
class MemoSQLHelper(context: Context, errorHandler: DatabaseErrorHandler)
    : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION, errorHandler
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(MemoStorage.SQL_CREATE_MEMO)
        db.execSQL(MemoStorage.SQL_CREATE_MEMO_IMAGE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(MemoStorage.SQL_DELETE_MEMO)
        db.execSQL(MemoStorage.SQL_DELETE_MEMO_IMAGE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    //---------------------------------------------------------------------------------------------- Memo
    /**
     * 메모를 추가한다.
     * @param title 메모 제목
     * @param contents 메모 내용
     * @return  저장된 key id
     */
    fun addMemo(title: String, contents: String, imageURL: List<String>?) {
        val contentValues = ContentValues()
        contentValues.put(MemoStorage.MemoTable.COLUMN_NAME_TITLE, title)
        contentValues.put(MemoStorage.MemoTable.COLUMN_NAME_CONTENTS, contents)
        if (!imageURL?.isEmpty()!!){
            contentValues.put(MemoStorage.MemoTable.COLUMN_NAME_THUMBNAIL, imageURL?.get(0))
        }else {
            contentValues.put(MemoStorage.MemoTable.COLUMN_NAME_THUMBNAIL, "")
        }

        val db = writableDatabase
        val memoId = db.insert(MemoStorage.MemoTable.TABLE_NAME, null, contentValues)
        addMemoImage(memoId, imageURL)
    }

    /**
     * 모든 메모 내용을 가져온다.
     * @return select 문에 대한 커서
     */
    fun getAllMemo(): Cursor {
        val db = readableDatabase
        val projection = arrayOf(BaseColumns._ID,
            MemoStorage.MemoTable.COLUMN_NAME_TITLE,
            MemoStorage.MemoTable.COLUMN_NAME_CONTENTS,
            MemoStorage.MemoTable.COLUMN_NAME_THUMBNAIL
        )

        return db.query(
            MemoStorage.MemoTable.TABLE_NAME, projection,
            null, null, null, null, null)
    }

    fun deleteMemo(memoId : Long) {
        val db = writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(memoId.toString())
        // Issue SQL statement.
        val deletedRows = db.delete(MemoStorage.MemoTable.TABLE_NAME, selection, selectionArgs)
        deleteMemoImage(memoId)
    }

    /**
     * 메모를 업데이트한다.
     */
    fun updateMemo(memoId : Long, title: String, contents: String, imageURL: List<String>?) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(MemoStorage.MemoTable.COLUMN_NAME_TITLE, title)
            put(MemoStorage.MemoTable.COLUMN_NAME_CONTENTS, contents)
            if (!imageURL?.isEmpty()!!) {
                put(MemoStorage.MemoTable.COLUMN_NAME_THUMBNAIL, imageURL?.get(0))
            }
            else{
                put(MemoStorage.MemoTable.COLUMN_NAME_THUMBNAIL, "")
            }
        }

        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(memoId.toString())
        db.update(MemoStorage.MemoTable.TABLE_NAME, values, selection, selectionArgs)

        deleteMemoImage(memoId)
        addMemoImage(memoId, imageURL)

    }

    //---------------------------------------------------------------------------------------------- MemoImage
    fun addMemoImage(memoId: Long, imageURL: List<String>?) {
        val db = writableDatabase
        if (imageURL != null) {
            for (url in imageURL) {
                val contentValues = ContentValues()
                contentValues.put(MemoStorage.MemoImageTable.COLUMN_NAME_MEMO_ID, memoId)
                contentValues.put(MemoStorage.MemoImageTable.COLUMN_NAME_IMAGE_URL, url)

                db.insert(MemoStorage.MemoImageTable.TABLE_NAME, null, contentValues)
            }
        }
    }

    fun getAllMemoImage(memoId : Long): Cursor {
        val db = readableDatabase

        val projection = arrayOf(BaseColumns._ID,
            MemoStorage.MemoImageTable.COLUMN_NAME_MEMO_ID,
            MemoStorage.MemoImageTable.COLUMN_NAME_IMAGE_URL
        )
        val selection = "${MemoStorage.MemoImageTable.COLUMN_NAME_MEMO_ID} = ?"
        val selectionArgs = arrayOf(memoId.toString())

        return db.query(
            MemoStorage.MemoImageTable.TABLE_NAME, projection,
            selection, selectionArgs, null, null, null)
    }

    fun deleteMemoImage(memoId : Long) {
        val db = writableDatabase
        val selection = "${MemoStorage.MemoImageTable.COLUMN_NAME_MEMO_ID} = ?"
        val selectionArgs = arrayOf(memoId.toString())
        val deletedRows = db.delete(MemoStorage.MemoImageTable.TABLE_NAME, selection, selectionArgs)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MemoStorage.db"
    }
}