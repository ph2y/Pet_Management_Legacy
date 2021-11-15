package com.sju18001.petmanagement.controller

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.marginTop
import com.sju18001.petmanagement.R
import java.lang.Integer.max

class CustomProgressBar {
    companion object{
        public fun addProgressBar(context: Context, view: View, size: Int): Int{
            val linearLayout = LinearLayout(context)
            linearLayout.layoutParams = view.layoutParams
            linearLayout.id = View.generateViewId() // id 지정 -> return
            linearLayout.translationZ = 4f // 맨 앞으로 보내기
            (view.parent as ViewGroup).addView(linearLayout)

            val progressBar = ProgressBar(context)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size) // size 지정
            params.gravity = Gravity.CENTER // 가운데 정렬
            progressBar.layoutParams = params
            linearLayout.addView(progressBar)

            return linearLayout.id
        }

        public fun addProgressBar(context: Context, view: View, size: Int, color: Int): Int{
            val id = addProgressBar(context, view, size)

            val linearLayout = (view.parent as ViewGroup).findViewById<LinearLayout>(id)
            linearLayout.setBackgroundColor(context.getColor(color)) // 배경색 지정

            return linearLayout.id
        }

        public fun removeProgressBar(context: Context, id:Int, view: View){
            val parent = view.parent as ViewGroup

            for(i in 0 until parent.childCount){
                val child = parent.getChildAt(i)
                if(child.id == id){
                    parent.removeView(child)
                }
            }
        }
    }
}