package com.zj.plugindemo.util

import android.content.Context
import android.os.Environment
import java.io.*

object AssetsUtil {

    fun copyFiles(context: Context, fileName: String): String {
        val cacheDir = getCacheDir(context)
        val filePath = cacheDir.absolutePath + File.separator + fileName
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
            copyFiles(context, fileName, file)
        }

        return filePath
    }

    private fun copyFiles(context: Context, fileName: String, desFile: File) {
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            inputStream = context.applicationContext.assets.open(fileName)
            out = FileOutputStream(desFile.absolutePath)
            val bytes = ByteArray(1024)
            var i: Int
            while (inputStream.read(bytes).also { i = it } != -1)
                out.write(bytes, 0, i)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getCacheDir(context: Context): File {
        val cache = if (hasExternalStorage()) {
            context.externalCacheDir!!
        } else {
            context.cacheDir
        }

        if (cache != null && !cache.exists()) {
            cache.mkdirs()
        }
        return cache
    }

    private fun hasExternalStorage(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}