package com.krllus.bitmappoc

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap

// https://stackoverflow.com/a/74814850

@Composable
fun BitmapComposable(
    onBitmapped: (bitmap: Bitmap) -> Unit = { _ -> },
    backgroundColor: Color = Color.Transparent,
    dpSize: DpSize,
    composable: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .size(0.dp, 0.dp)
            .verticalScroll(
                rememberScrollState(), enabled = false
            )
            .horizontalScroll(
                rememberScrollState(), enabled = false
            )
    ) {

        Box(modifier = Modifier.size(dpSize)) {
            AndroidView(factory = {
                ComposeView(it).apply {
                    setContent {
                        Box(modifier = Modifier.background(backgroundColor).fillMaxSize()) {
                            composable()
                        }
                    }
                }
            }, modifier = Modifier.fillMaxSize(), update = {
                it.run {
                    doOnLayout {
                        onBitmapped(drawToBitmap())
                    }
                }
            })
        }
    }

}

@Composable
fun BitmapComposable(
    onBitmapped: (bitmap: Bitmap) -> Unit = { _ -> },
    backgroundColor: Color = Color.Transparent,
    intSize: IntSize, // Pixel size for output bitmap
    composable: @Composable () -> Unit
) {
    val renderComposableSize = LocalDensity.current.run { intSize.toSize().toDpSize() }
    BitmapComposable(onBitmapped, backgroundColor, renderComposableSize, composable)
}