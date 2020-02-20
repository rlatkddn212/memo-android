package com.ksw.memo

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerMemoItemClickListener (context : Context, recyclerView: RecyclerView, private val listener : OnRecyclerClickListener)
    : RecyclerView.SimpleOnItemTouchListener() {
    private val TAG = "RecyclerMemoItemClick"

    interface OnRecyclerClickListener {
        fun onItemClick(view: View, position : Int)
    }

    private val gestureDetector = GestureDetectorCompat(context, object: GestureDetector.SimpleOnGestureListener(){
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.d(TAG, "onSingleTapUp start")
            val childView = recyclerView.findChildViewUnder(e.x, e.y)
            Log.d(TAG, "onSingleTapUp onItemClick")
            if (childView != null)
                listener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView))

            return true
        }
    })

    /**
     * onInterceptTouchEvent 는 하위 view로 이벤트를 전달 결정
     */
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptTouchEvent() - MotionEvent $e")
        val result = gestureDetector.onTouchEvent(e)
        Log.d(TAG, "onInterceptTouchEvent() result : $result")
        return result
    }
}