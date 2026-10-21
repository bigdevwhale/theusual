package app.theusual.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/**
 * A photo of a café front: kept as it is (scaled down), or redrawn as an ink sketch on cream
 * paper, the edges in brown ink and the colours washed out like watercolour. Without one, the page
 * shows a front drawn from memory.
 */
object Sketches {
    private const val SIDE = 960

    fun dir(c: Context) = File(c.filesDir, "sketches").apply { mkdirs() }

    fun file(c: Context, name: String?): File? = name?.let { File(dir(c), it) }?.takeIf { it.exists() }

    fun delete(c: Context, name: String?) {
        file(c, name)?.delete()
    }

    /** Is this file the photo as it was taken, not a sketch? */
    fun isPhoto(name: String?) = name?.startsWith("photo-") == true

    suspend fun fromPhoto(c: Context, uri: Uri, ink: Boolean): String? = withContext(Dispatchers.Default) {
        runCatching {
            val photo = ImageDecoder.decodeBitmap(ImageDecoder.createSource(c.contentResolver, uri)) { d, info, _ ->
                val s = info.size
                val k = max(1, max(s.width, s.height) / (SIDE * 2))
                d.setTargetSampleSize(k)
                d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
            val out = if (ink) draw(photo) else scaled(photo, 1280)
            val name = (if (ink) "sketch-" else "photo-") + UUID.randomUUID().toString() + ".jpg"
            File(dir(c), name).outputStream().use { out.compress(Bitmap.CompressFormat.JPEG, 88, it) }
            name
        }.getOrNull()
    }

    private fun scaled(src: Bitmap, side: Int): Bitmap {
        val k = side.toFloat() / max(src.width, src.height)
        return if (k < 1f) Bitmap.createScaledBitmap(src, (src.width * k).toInt(), (src.height * k).toInt(), true) else src
    }

    /** Ink lines from a difference of blurs, over a pale, warm wash of the photo's colours. */
    fun draw(src: Bitmap): Bitmap {
        val scale = SIDE.toFloat() / max(src.width, src.height)
        val b = if (scale < 1f) Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true) else src
        val w = b.width
        val h = b.height
        val px = IntArray(w * h)
        b.getPixels(px, 0, w, 0, 0, w, h)
        val lum = FloatArray(w * h) { i ->
            val p = px[i]
            (0.299f * (p shr 16 and 255) + 0.587f * (p shr 8 and 255) + 0.114f * (p and 255)) / 255f
        }
        val fine = blur(lum, w, h, 1)
        val wide = blur(lum, w, h, 5)
        val out = IntArray(w * h)
        val paperR = 246f; val paperG = 236f; val paperB = 214f
        val inkR = 52f; val inkG = 38f; val inkB = 28f
        for (i in out.indices) {
            val p = px[i]
            // The wash: the colour, desaturated toward warm paper and posterised a little.
            val l = lum[i]
            var r = (p shr 16 and 255) * 0.45f + l * 255f * 0.55f
            var g = (p shr 8 and 255) * 0.45f + l * 255f * 0.55f
            var bl = (p and 255) * 0.45f + l * 255f * 0.55f
            r = (r / 48).toInt() * 48f + 24; g = (g / 48).toInt() * 48f + 24; bl = (bl / 48).toInt() * 48f + 24
            val wash = 0.34f
            r = paperR * (1 - wash) + r * wash
            g = paperG * (1 - wash) + g * wash
            bl = paperB * (1 - wash) + bl * wash
            // The ink: where the fine detail is darker than its surroundings.
            val edge = wide[i] - fine[i]
            val ink = ((edge - 0.035f) / 0.07f).coerceIn(0f, 1f) * 0.92f
            val shade = ((0.32f - l) / 0.32f).coerceIn(0f, 1f) * 0.35f
            val a = min(1f, ink + shade)
            r = r * (1 - a) + inkR * a
            g = g * (1 - a) + inkG * a
            bl = bl * (1 - a) + inkB * a
            out[i] = (0xFF shl 24) or (r.toInt().coerceIn(0, 255) shl 16) or (g.toInt().coerceIn(0, 255) shl 8) or bl.toInt().coerceIn(0, 255)
        }
        return Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888)
    }

    /** A box blur, horizontal then vertical. */
    private fun blur(src: FloatArray, w: Int, h: Int, r: Int): FloatArray {
        val tmp = FloatArray(src.size)
        val out = FloatArray(src.size)
        val n = 2 * r + 1
        for (y in 0 until h) {
            var sum = 0f
            for (x in -r..r) sum += src[y * w + x.coerceIn(0, w - 1)]
            for (x in 0 until w) {
                tmp[y * w + x] = sum / n
                sum += src[y * w + (x + r + 1).coerceAtMost(w - 1)] - src[y * w + (x - r).coerceAtLeast(0)]
            }
        }
        for (x in 0 until w) {
            var sum = 0f
            for (y in -r..r) sum += tmp[y.coerceIn(0, h - 1) * w + x]
            for (y in 0 until h) {
                out[y * w + x] = sum / n
                sum += tmp[(y + r + 1).coerceAtMost(h - 1) * w + x] - tmp[(y - r).coerceAtLeast(0) * w + x]
            }
        }
        return out
    }
}
