package app.theusual

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.theusual.data.Sketches
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * The ink sketch of a real photo, for a look (run with `-Prender` and SKETCH_IN pointing at a
 * JPEG of a café front): writes the photo's sketch next to the screen renders.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class RenderArtSketch {
    @Test fun sketch() {
        val input = System.getenv("SKETCH_IN")?.let(::File)
        assumeTrue(input != null && input.exists())
        val photo = BitmapFactory.decodeFile(input!!.path)
        val out = File(System.getProperty("render.out") ?: "build/art").apply { mkdirs() }
        File(out, "sketch.png").outputStream().use { Sketches.draw(photo).compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
