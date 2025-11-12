package com.example.tallerfinal.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * Utilidad para crear marcadores personalizados del mapa
 * Genera marcadores circulares con fotos de perfil de usuarios
 * Maneja la carga de imágenes desde URLs y la conversión a bitmaps circulares
 */
object MarkerUtils {

    suspend fun createCustomMarker(
        context: Context,
        imageUrl: String?,
        isCurrentUser: Boolean = false
    ): BitmapDescriptor = withContext(Dispatchers.IO) {

        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val profileBitmap = if (!imageUrl.isNullOrEmpty()) {
            loadImageFromUrl(context, imageUrl, size)
        } else {
            null
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val borderColor = if (isCurrentUser) {
            android.graphics.Color.BLUE
        } else {
            android.graphics.Color.RED
        }

        paint.color = borderColor
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 6, paint)

        if (profileBitmap != null) {
            val circularBitmap = getCircularBitmap(profileBitmap)
            val scaledBitmap = Bitmap.createScaledBitmap(
                circularBitmap,
                size - 12,
                size - 12,
                true
            )
            canvas.drawBitmap(scaledBitmap, 6f, 6f, null)
        } else {
            paint.color = borderColor
            paint.alpha = 100
            canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 10, paint)
        }

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private suspend fun loadImageFromUrl(
        context: Context,
        url: String,
        size: Int
    ): Bitmap? {
        return try {
            android.util.Log.d("MarkerUtils", "Intentando cargar imagen: $url")
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(size)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                if (drawable is android.graphics.drawable.BitmapDrawable) {
                    android.util.Log.d("MarkerUtils", "Imagen cargada exitosamente")
                    drawable.bitmap
                } else {
                    android.util.Log.w("MarkerUtils", "Drawable no es BitmapDrawable")
                    null
                }
            } else {
                android.util.Log.w("MarkerUtils", "Error al cargar imagen")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("MarkerUtils", "Excepción al cargar imagen: ${e.message}")
            null
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }
}

