package com.sju18001.petmanagement.restapi

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class ServerUtil {
    companion object{
        // for copying selected image
        fun createCopyAndReturnRealPath(context: Context, uri: Uri): String {
            val newFilePath = context.applicationInfo.dataDir + File.separator + System.currentTimeMillis()
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
    }
}