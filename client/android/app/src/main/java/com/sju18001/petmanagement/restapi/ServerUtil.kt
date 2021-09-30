package com.sju18001.petmanagement.restapi

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.sju18001.petmanagement.controller.Util
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ServerUtil {
    companion object{
        fun <T> enqueueApiCall(
            call: Call<T>,
            isViewDestroyed: Boolean,
            context: Context,
            onSuccessful: (Response<T>)->Unit,
            onNotSuccessful: (Response<T>)->Unit,
            onFailure: ()->Unit
        ){
            call.enqueue(object: Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if(isViewDestroyed) return

                    if(response.isSuccessful){
                        onSuccessful.invoke(response)
                    }else{
                        onNotSuccessful.invoke(response)
                        Util.showToastAndLogForFailedResponse(context, response.errorBody())
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    if(isViewDestroyed) return

                    onFailure.invoke()
                    Util.showToastAndLog(context, t.message.toString())
                }
            })
        }

        fun getEmptyBody(): RequestBody{
            return RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")
        }

        fun createCopyAndReturnRealPathLocal(context: Context, uri: Uri, directory: String): String {
            val mimeTypeMap = MimeTypeMap.getSingleton()
            val extension = mimeTypeMap.getExtensionFromMimeType(context.contentResolver.getType(uri))!!

            val baseDirectory = context.getExternalFilesDir(null).toString() + File.separator + directory
            if(!File(baseDirectory).exists()) { File(baseDirectory).mkdir() }

            val newFilePath = baseDirectory + File.separator + System.currentTimeMillis() + '.' + extension
            val newFile = File(newFilePath)

            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(newFile)
            val buffer = ByteArray(1024)

            var len: Int
            while (inputStream!!.read(buffer).also { len = it } > 0) outputStream.write(buffer, 0, len)
            outputStream.close()
            inputStream.close()

            return newFile.absolutePath
        }

        fun createCopyAndReturnRealPathServer(context: Context, byteArray: ByteArray, extension: String, directory: String): String {
            val baseDirectory = context.getExternalFilesDir(null).toString() + File.separator + directory
            if(!File(baseDirectory).exists()) { File(baseDirectory).mkdir() }

            val newFilePath = baseDirectory + File.separator + System.currentTimeMillis() + '.' + extension
            val newFile = File(newFilePath)

            val outputStream = FileOutputStream(newFile)
            outputStream.write(byteArray)
            outputStream.close()

            return newFile.absolutePath
        }
    }
}