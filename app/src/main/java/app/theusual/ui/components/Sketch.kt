package app.theusual.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.data.Sketches
import app.theusual.ui.art.Art
import app.theusual.ui.art.FacadeSketch
import app.theusual.ui.art.Tape
import app.theusual.ui.art.laid
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A sketch from the sketches folder, loaded off the main thread; null while loading or missing. */
@Composable
fun rememberSketch(name: String?): ImageBitmap? {
    val c = ctx()
    val image by produceState<ImageBitmap?>(null, name) {
        value = withContext(Dispatchers.IO) { Sketches.file(c, name)?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() } }
    }
    return image
}

/**
 * The café's front on a print taped into the notebook: the ink sketch made from a photo, or one
 * drawn from memory (in the packet's colours) when there's no photo.
 */
@Composable
fun Polaroid(sketch: String?, palette: Int, sign: String, caption: String, modifier: Modifier = Modifier, angle: Float = -2.5f) {
    val image = rememberSketch(sketch)
    Box(modifier.rotate(angle)) {
        Column(Modifier.laid().background(androidx.compose.ui.graphics.Color(0xFFFBF3E2)).padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp)) {
            if (image != null) {
                Image(image, null, Modifier.fillMaxWidth().aspectRatio(1.28f), contentScale = ContentScale.Crop)
            } else {
                FacadeSketch(Art.packet(palette).first, Modifier.fillMaxWidth().aspectRatio(1.38f), sign)
            }
            Text(caption, fontFamily = Hand, fontSize = 18.sp, color = androidx.compose.ui.graphics.Color(0xFF7A6A55), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp))
        }
        Tape(Modifier.align(Alignment.TopCenter).offset(y = (-10).dp), w = 96.dp, h = 22.dp)
    }
}
