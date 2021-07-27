package com.example.workmanager

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.IOException
import java.net.URL

import java.io.FileOutputStream

import java.io.OutputStream
import java.util.*

fun URL.toBitmap(): Bitmap? {
    return try {
        BitmapFactory.decodeStream(openStream())
    } catch (e: IOException) {
        null
    }
}

fun Bitmap.saveToInternalStorage(context: Context): Uri? {
    val wrapper = ContextWrapper(context)
    var file = wrapper.getDir("images", Context.MODE_PRIVATE)
    file = File(file, "${UUID.randomUUID()}.jpg")
    return try {
        val stream: OutputStream = FileOutputStream(file)
        compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        Uri.parse(file.absolutePath)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Context.getUriFromUrl(): Uri? {
    val imageUrl =
        URL(
            "https://images.pexels.com/photos/169573/pexels-photo-169573.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940"
        )
    val bitmap = imageUrl.toBitmap()
    var savedUri: Uri? = null
    bitmap?.apply {
        savedUri = saveToInternalStorage(this@getUriFromUrl)
    }
    return savedUri
}