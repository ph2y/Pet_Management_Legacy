package com.sju18001.petmanagement.restapi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.sju18001.petmanagement.controller.Util
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ServerUtil {
    companion object{
        const val WRITE_REQUEST_CODE = 0

        fun <T> enqueueApiCall(
            call: Call<T>,
            getIsViewDestroyed: ()-> Boolean,
            context: Context,
            onSuccessful: (Response<T>)->Unit,
            onNotSuccessful: (Response<T>)->Unit,
            onFailure: (t: Throwable)->Unit
        ){
            call.enqueue(object: Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if(getIsViewDestroyed()) return

                    if(response.isSuccessful){
                        onSuccessful.invoke(response)
                    }else{
                        onNotSuccessful.invoke(response)
                        Util.showToastAndLogForFailedResponse(context, response.errorBody())
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    if(getIsViewDestroyed()) return

                    onFailure.invoke(t)
                    Util.showToastAndLog(context, t.message.toString())
                }
            })
        }

        fun getEmptyBody(): RequestBody{
            return RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")
        }

        fun createCopyAndReturnRealPathLocal(context: Context, uri: Uri, directory: String, fileName: String): String {
            val mimeTypeMap = MimeTypeMap.getSingleton()
            if (mimeTypeMap.getExtensionFromMimeType(context.contentResolver.getType(uri)) == null) {
                return ""
            }

            val baseDirectory = context.getExternalFilesDir(null).toString() + File.separator + directory
            if(!File(baseDirectory).exists()) { File(baseDirectory).mkdir() }

            val newFilePath = baseDirectory + File.separator + fileName
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

        fun createCopyAndReturnContentUri(context: Context, byteArray: ByteArray, extension: String, directory: String): Uri {
            val baseDirectory = context.getExternalFilesDir(null).toString() + File.separator + directory
            if(!File(baseDirectory).exists()) { File(baseDirectory).mkdir() }

            val newFilePath = baseDirectory + File.separator + System.currentTimeMillis() + '.' + extension
            val newFile = writeAndGetFile(byteArray, newFilePath)

            return FileProvider.getUriForFile(context, "${context.applicationInfo.packageName}.fileprovider", newFile)
        }

        fun createCopyAndReturnRealPathServer(context: Context, byteArray: ByteArray, extension: String, directory: String): String {
            val baseDirectory = context.getExternalFilesDir(null).toString() + File.separator + directory
            if(!File(baseDirectory).exists()) { File(baseDirectory).mkdir() }

            val newFilePath = baseDirectory + File.separator + System.currentTimeMillis() + '.' + extension
            val newFile = writeAndGetFile(byteArray, newFilePath)

            return newFile.absolutePath
        }

        fun writeAndGetFile(byteArray: ByteArray, filePath: String): File{
            val newFile = File(filePath)

            val outputStream = FileOutputStream(newFile)
            outputStream.write(byteArray)
            outputStream.close()

            return newFile
        }

        fun getUriFromUser(activity: Activity, fileName: String) {
            // get MIME type
            val mimeTypeMap = MimeTypeMap.getSingleton()
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(fileName.split('.').last())

            // get Uri from user
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = mimeType
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            activity.startActivityForResult(intent, WRITE_REQUEST_CODE)
        }

        fun writeFileToUri(context: Context, downloadedFilePath: String, uri: Uri) {
            context.contentResolver.openFileDescriptor(uri, "w").use { pfd->
                FileOutputStream(pfd!!.fileDescriptor).use { fos ->
                    val inputStream = context.contentResolver.openInputStream(Uri.fromFile(File(downloadedFilePath)))
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream!!.read(buffer).also { len = it } > 0) fos.write(buffer, 0, len)
                    fos.close()
                    inputStream.close()
                }
            }
        }
    }
}