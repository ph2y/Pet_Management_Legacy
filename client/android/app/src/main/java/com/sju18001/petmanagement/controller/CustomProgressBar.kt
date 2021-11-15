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
        public fun addProgressBar(context: Context, view: View, size: Int){
            view.post{
                val linearLayout = LinearLayout(context)
                linearLayout.layoutParams = view.layoutParams
                linearLayout.translationZ = 4f // 맨 앞으로 보내기
                linearLayout.tag = "CustomProgressBar"
                (view as ViewGroup).addView(linearLayout)

                val progressBar = ProgressBar(context)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size) // size 지정
                params.gravity = Gravity.CENTER // 가운데 정렬
                progressBar.layoutParams = params
                linearLayout.addView(progressBar)
            }
        }

        public fun addProgressBar(context: Context, view: View, size: Int, color: Int){
            view.post{
                val linearLayout = LinearLayout(context)
                linearLayout.layoutParams = view.layoutParams
                linearLayout.translationZ = 4f // 맨 앞으로 보내기
                linearLayout.setBackgroundColor(context.getColor(color)) // 배경색 지정
                linearLayout.tag = "CustomProgressBar"
                (view as ViewGroup).addView(linearLayout)

                val progressBar = ProgressBar(context)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size) // size 지정
                params.gravity = Gravity.CENTER // 가운데 정렬
                progressBar.layoutParams = params
                linearLayout.addView(progressBar)
            }
        }

        public fun removeProgressBar(view: View){
            val viewGroup = (view as ViewGroup)

            for(i in 0 until viewGroup.childCount){
                val child = viewGroup.getChildAt(i)
                if(child.tag == "CustomProgressBar"){
                    viewGroup.removeView(child)
                }
            }
        }
    }
}