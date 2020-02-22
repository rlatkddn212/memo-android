package com.ksw.memo.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ksw.memo.R
import com.squareup.picasso.Picasso

//-------------------------------------------------------------------------------------------------- ImageEditRecyclerViewHolder
class ImageEditRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var editImage: ImageView = view.findViewById(R.id.edit_image)
    var editPathText : TextView = view.findViewById(R.id.edit_path_text)
}

//-------------------------------------------------------------------------------------------------- ImageEditRecyclerViewAdapter
class ImageEditRecyclerViewAdapter (val imageList: List<String>): RecyclerView.Adapter<ImageEditRecyclerViewHolder>() {
    private val TAG = "ImageEditRecyclerView"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageEditRecyclerViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memo_image_edit, parent, false)
        return ImageEditRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun getMemo(position: Int): String? {
        return if (imageList.isNotEmpty()) {
            imageList[position]
        } else null
    }

    override fun onBindViewHolder(holder: ImageEditRecyclerViewHolder, position: Int) {
        val image = imageList[position]
        Log.d(TAG, "onBindViewHolder : ${image} - $position")

        if (image != null) {
            Picasso.get()
                .load(Uri.parse(image))
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.placeholder)
                .into(holder.editImage)
        }

        holder.editPathText.text = image
    }
}
