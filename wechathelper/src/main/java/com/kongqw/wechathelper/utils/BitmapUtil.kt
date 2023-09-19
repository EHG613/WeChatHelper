package com.kongqw.wechathelper.utils

import android.graphics.Bitmap
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

    fun bitmapToByteArray2(
        bmp: Bitmap,
        maxSize: Int,
        isWithAlpha: Boolean = false
    ): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        //压缩的格式（如果被压缩的图片带有透明度需要选择PNG)
        var compressFormat = Bitmap.CompressFormat.JPEG
        if (isWithAlpha) {
            compressFormat = Bitmap.CompressFormat.PNG
        }
        //压缩的质量（压缩质量范围0-100，值越大，质量越好，对于 PNG 和 WEBP 格式，此参数不起作用，可以设置为 0）
        var compressQuality = 100
        //将位图压缩为指定格式
        bmp.compress(compressFormat, compressQuality, byteArrayOutputStream)
        //判断压缩后的输出流是否符合指定的条件
        while ((byteArrayOutputStream.size() / 1024) > maxSize) {
            compressQuality -= 10
            if (compressQuality <= 0) {
                break
            }
            byteArrayOutputStream.reset()
            bmp.compress(compressFormat, compressQuality, byteArrayOutputStream)
        }
        return byteArrayOutputStream.toByteArray()
    }
}