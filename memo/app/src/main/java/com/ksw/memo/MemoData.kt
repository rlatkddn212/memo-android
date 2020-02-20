package com.ksw.memo

import android.os.Parcel
import android.os.Parcelable

data class MemoData(var title: String? = "", var contents : String?= "", var imageURL: List<String>? = null) :Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(contents)
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