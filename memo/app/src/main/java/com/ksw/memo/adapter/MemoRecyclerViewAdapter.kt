/**
 * 메모 리스트의 리사이클러 뷰
 * Picasso 라이브러리를 사용하여 표시된다.
 */
package com.ksw.memo.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ksw.memo.MemoData
import com.ksw.memo.R
import com.squareup.picasso.Picasso

//-------------------------------------------------------------------------------------------------- MemoRecyclerViewHolder
class MemoRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var title: TextView = view.findViewById(R.id.title)
    var contents : TextView = view.findViewById(R.id.contents)
    var memo_thumbnail: ImageView = view.findViewById(R.id.memo_thumbnail)
}

//-------------------------------------------------------------------------------------------------- MemoRecyclerViewAdapter
class MemoRecyclerViewAdapter(val memoList: List<MemoData>) :
    RecyclerView.Adapter<MemoRecyclerViewHolder>() {
    private val TAG = "MemoRecyclerViewAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoRecyclerViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memo_item, parent, false)
        return MemoRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return memoList.size
    }

    fun getMemo(position: Int): MemoData? {
        return if (memoList.isNotEmpty()) {
            memoList[position]
        } else null
    }

    override fun onBindViewHolder(holder: MemoRecyclerViewHolder, position: Int) {
        val memo = memoList[position]
        Log.d(TAG, "onBindViewHolder : ${memo} - $position")

        if (memo.thumbnail != null)
        {
            Picasso.get()
                .load(Uri.parse(memo.thumbnail))
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.brokenimage)
                .into(holder.memo_thumbnail)
        }
        holder.title.text = memo.title
        holder.contents.text = memo.contents
    }
}