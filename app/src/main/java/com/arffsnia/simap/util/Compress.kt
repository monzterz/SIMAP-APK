package com.arffsnia.simap.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Kecilkan foto (turunan sampel + JPEG kualitas 70, maks ~1600px).
 * Balik path file hasil (file asli dihapus), atau null bila gagal —
 * caller boleh lanjut pakai file asli.
 */
fun compressImage(srcPath: String): String? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(srcPath, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val bmp = BitmapFactory.decodeFile(srcPath, opts) ?: return null
        val src = File(srcPath)
        val dst = File(src.parent, "c_${System.currentTimeMillis()}.jpg")
        val out = FileOutputStream(dst)
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, out)
        out.flush()
        out.close()
        bmp.recycle()
        src.delete()
        dst.absolutePath
    } catch (e: Throwable) {
        null
    }
}