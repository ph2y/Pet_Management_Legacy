package com.sju18001.petmanagement.controller

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.sju18001.petmanagement.restapi.Place
import com.sju18001.petmanagement.restapi.global.FileMetaData
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class Util {
    companion object{
        fun convertDpToPixel(dp: Int): Int{
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics).toInt()
        }

        fun convertPixelToDp(context: Context, px: Int): Int{
            return px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun hideKeyboard(activity: Activity){
            activity.currentFocus?.let{
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.hideSoftInputFromWindow(it.windowToken, 0)
                it.clearFocus()
            }
        }

        fun showKeyboard(activity: Activity, view: EditText){
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            view.requestFocus()
            imm.showSoftInput(view, 0)

            // Set cursor at the end of view
            view.setSelection(view.text.toString().length)
        }

        fun setupViewsForHideKeyboard(activity: Activity, view: View) {
            // Set up touch listener for non-text box views to hide keyboard
            if(view !is EditText) {
                view.setOnTouchListener { _, _ ->
                    hideKeyboard(activity)
                    false
                }
            }

            // If a layout container, iterate over children and seed recursion
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val innerView = view.getChildAt(i)
                    setupViewsForHideKeyboard(activity, innerView)
                }
            }
        }

        // * Location Information
        fun openWebPage(activity: Activity, url: String){
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }

            activity.startActivity(intent)
        }

        fun doCall(activity: Activity, phone: String){
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }

            activity.startActivity(intent)
        }

        fun insertContactsContract(activity: Activity, document: Place) {
            val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE

                putExtra(ContactsContract.Intents.Insert.NAME, document.place_name)
                putExtra(ContactsContract.Intents.Insert.PHONE, document.phone)
            }

            activity.startActivity(intent)
        }

        fun doCopy(context: Context, str: String){
            // 복사
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData: ClipData = ClipData.newPlainText("A phone number", str)

            clipboard.setPrimaryClip(clipData)

            // 토스트 메시지
            Toast.makeText(context, "클립보드에 복사되었습니다.", Toast.LENGTH_LONG).show()
        }

        fun shareText(activity: Activity, value: String){
            val sendIntent: Intent = Intent().apply{
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, value)
                type= "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            activity.startActivity(shareIntent)
        }

        fun getMessageFromErrorBody(errorBody: ResponseBody): String{
            val metadata = JSONObject(errorBody.string().trim()).getString("_metadata")
            return JSONObject(metadata).getString("message")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getSecondDifferenceInLocalDateTime(localDateTime: LocalDateTime): Long{
            val time = localDateTime.atZone(ZoneId.of("Asia/Seoul"))
            val now = LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul"))

            return kotlin.math.abs(now.toEpochSecond() - time.toEpochSecond())
        }

        fun isUrlVideo(url: String): Boolean{
            return url.endsWith(".mp4") || url.endsWith(".webm")
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun getScreenWidthInPixel(activity: Activity) : Int{
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.width() - insets.left - insets.right
        }

        fun deleteCopiedFiles(context: Context, directory: String) {
            val dir = File(context.getExternalFilesDir(null).toString() + File.separator + directory)
            dir.deleteRecursively()
        }

        fun getTemporaryFilesSize(context: Context): Long {
            // set File path
            val file = File(context.getExternalFilesDir(null).toString())

            // no such file/directory exception
            if (!file.exists()) {
                return 0
            }
            // if file
            if (!file.isDirectory) {
                return file.length()
            }

            // if directory
            val dirs: MutableList<File> = LinkedList()
            dirs.add(file)

            var result: Long = 0
            while (dirs.isNotEmpty()) {
                val dir = dirs.removeAt(0)
                if (!dir.exists()) {
                    continue
                }
                val listFiles = dir.listFiles()
                if (listFiles.isNullOrEmpty()) {
                    continue
                }
                for (child in listFiles) {
                    result += child.length()
                    if (child.isDirectory) dirs.add(child)
                }
            }

            return result
        }

        fun getArrayFromMediaAttachments(mediaAttachments: String?): Array<FileMetaData>{
            return if (mediaAttachments != null) Gson().fromJson(mediaAttachments, Array<FileMetaData>::class.java) else arrayOf()
        }

        fun showToastAndLogForFailedResponse(context: Context, errorBody: ResponseBody?){
            if(errorBody == null) return

            val errorMessage = getMessageFromErrorBody(errorBody)
            showToastAndLog(context, errorMessage)
        }

        fun showToastAndLog(context: Context, message: String){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            Log.d("error", message)
        }
    }
}