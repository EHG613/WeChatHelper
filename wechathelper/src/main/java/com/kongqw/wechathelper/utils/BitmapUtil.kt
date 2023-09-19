package com.kongqw.wechathelper.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import java.io.ByteArrayOutputStream


internal object BitmapUtil {

    /**
     * Bitmap 转 ByteArray
     */
    fun bitmapToByteArray(bmp: Bitmap, needRecycle: Boolean): ByteArray? {
        val output = ByteArrayOutputStream()
        return try {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, output)
            if (needRecycle) {
                bmp.recycle()
            }
            output.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            output.close()
        }
    }

    fun bitmapToByteArray2(bmp: Bitmap, needRecycle: Boolean): ByteArray {
        var i: Int
        var j: Int
        if (bmp.height > bmp.width) {
            i = bmp.width
            j = bmp.width
        } else {
            i = bmp.height
            j = bmp.height
        }
        val localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565)
        val localCanvas = Canvas(localBitmap)
        while (true) {
            localCanvas.drawBitmap(bmp, Rect(0, 0, i, j), Rect(0, 0, i, j), null)
            if (needRecycle) bmp.recycle()
            val localByteArrayOutputStream = ByteArrayOutputStream()
            localBitmap.compress(
                Bitmap.CompressFormat.JPEG, 100,
                localByteArrayOutputStream
            )
            localBitmap.recycle()
            val arrayOfByte = localByteArrayOutputStream.toByteArray()
            try {
                localByteArrayOutputStream.close()
                return arrayOfByte
            } catch (e: java.lang.Exception) {
                // F.out(e);
            }
            i = bmp.height
            j = bmp.height
        }
    }
}