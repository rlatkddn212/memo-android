package com.ksw.memo

import android.os.Parcel
import android.os.Parcelable

/**
 * 데이터 클래스
 * 메모에 데이터를 저장한다.
 *
 * memoId : db에 저장시 자동 증가하는 unique id
 * title : 메모의 제목
 * contents : 메모의 내용
 * thumbnail : 썸네일 이미지의 URL
 * imageURL : 이미지 URL 리스트
 */

//-------------------------------------------------------------------------------------------------- MemoData
data class MemoData(var memoId :Long = -1L, var title: String? = "", var contents : String?= "",
                    var thumbnail: String? = "", var imageURL: MutableList<String>? = ArrayList()) :Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(memoId)
        parcel.writeString(title)
        parcel.writeString(contents)
        parcel.writeString(thumbnail)
        parcel.writeStringList(imageURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MemoData> {
        override fun createFromParcel(parcel: Parcel): MemoData {
            return MemoData(parcel)
        }

        override fun newArray(size: Int): Array<MemoData?> {
            return arrayOfNulls(size)
        }
    }
}