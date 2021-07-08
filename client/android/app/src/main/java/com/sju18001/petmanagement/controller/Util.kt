package com.sju18001.petmanagement.controller

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.provider.ContactsContract
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.sju18001.petmanagement.restapi.Place

class Util {
    public fun convertDpToPixel(pixel: Int): Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel.toFloat(), Resources.getSystem().displayMetrics).toInt()
    }

    public fun hideKeyboard(activity: Activity, view: View){
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    // * Location Information
    public fun openWebPage(activity: Activity, url: String){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }

        activity.startActivity(intent)
    }

    public fun doCall(activity: Activity, phone: String){
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }

        activity.startActivity(intent)
    }

    public fun insertContactsContract(activity: Activity, document: Place) {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE

            putExtra(ContactsContract.Intents.Insert.NAME, document.place_name)
            putExtra(ContactsContract.Intents.Insert.PHONE, document.phone)
        }

        activity.startActivity(intent)
    }

    public fun doCopy(context: Context, str: String){
        // 복사
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData: ClipData = ClipData.newPlainText("A phone number", str)

        clipboard.setPrimaryClip(clipData)

        // 토스트 메시지
        Toast.makeText(context, "클립보드에 복사되었습니다.", Toast.LENGTH_LONG).show()
    }

    public fun shareText(activity: Activity, value: String){
        val sendIntent: Intent = Intent().apply{
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, value)
            type= "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        activity.startActivity(shareIntent)
    }
}