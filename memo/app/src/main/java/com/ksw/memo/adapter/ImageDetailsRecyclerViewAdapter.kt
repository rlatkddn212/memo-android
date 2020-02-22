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

//-------------------------------------------------------------------------------------------------- ImageDetailsRecyclerViewHolder
class ImageDetailsRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var detailsImage: ImageView = view.findViewById(R.id.details_image)
    var detailsPathText : TextView = view.findViewById(R.id.details_path_text)
}

//-------------------------------------------------------------------------------------------------- ImageDetailsRecyclerViewAdapter
class ImageDetailsRecyclerViewAdapter (val imageList: List<String>)
    : RecyclerView.Adapter<ImageDetailsRecyclerViewHolder>() {
    private val TAG = "ImageDetailsRecycler"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageDetailsRecyclerViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memo_image_details, parent, false)
        return ImageDetailsRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun getMemo(position: Int): String? {
        return if (imageList.isNotEmpty()) {
            imageList[position]
        } else null
    }

    override fun onBindViewHolder(holder: ImageDetailsRecyclerViewHolder, position: Int) {
        val image = imageList[position]
        Log.d(TAG, "onBindViewHolder : ${image} - $position")

        if (image != null) {
            Picasso.get()
                .load(Uri.parse(image))
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.placeholder)
                .into(holder.detailsImage)
        }

        holder.detailsPathText.text = image
    }
}
