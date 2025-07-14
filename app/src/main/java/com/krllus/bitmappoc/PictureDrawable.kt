package com.krllus.bitmappoc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun Something() {

    // check https://stackoverflow.com/a/77597340

    val picture = remember { Picture() }

    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column {
        Row {
            Button(onClick = {
                val bitmap = createBitmapFromPicture(picture)
                coroutineScope.launch {
                    bitmap.saveToDisk(ctx)
                }
            }) {
                Text("Click Me")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    // Example that shows how to redirect rendering to an Android Picture and then
                    // draw the picture into the original destination
                    val width = this.size.width.toInt()
                    val height = this.size.height.toInt()
                    onDrawWithContent {
                        val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                            picture.beginRecording(
                                width, height
                            )
                        )
                        draw(this, this.layoutDirection, pictureCanvas, this.size) {
                            this@onDrawWithContent.drawContent()
                        }
                        picture.endRecording()

                        drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
                    }
                }) {
            ScreenContentToCapture()
        }
    }
}

fun createBitmapFromPicture(picture: Picture): Bitmap {
    val bitmap = createBitmap(picture.width, picture.height)

    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawPicture(picture)
    return bitmap
}

suspend fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "screenshot-${System.currentTimeMillis()}.png"
    )

    println("myFilePathIs: ${file.absolutePath}")

    file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}
