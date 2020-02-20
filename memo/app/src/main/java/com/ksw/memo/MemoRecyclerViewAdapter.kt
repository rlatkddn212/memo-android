package com.ksw.memo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MemoRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var title: TextView = view.findViewById(R.id.title)
    var contents : TextView = view.findViewById(R.id.contents)
    var thumbnail: ImageView = view.findViewById(R.id.thumbnail)
}

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

    override fun onBindViewHolder(holder: MemoRecyclerViewHolder, position: Int) {
        val memo = memoList[position]
        Log.d(TAG, "onBindViewHolder : ${memo} - $position")

        Picasso.get()
            .load(memo.imageURL!![0])
            .error(R.drawable.placeholder)
            .placeholder(R.drawable.placeholder)
            .into(holder.thumbnail)

        holder.title.text = memo.title
        holder.contents.text = memo.contents
    }
}