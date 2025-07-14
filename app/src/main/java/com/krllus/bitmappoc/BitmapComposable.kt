package com.krllus.bitmappoc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.applyCanvas
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap

// https://stackoverflow.com/a/74814850

@Composable
fun BitmapComposable(
    onBitmapped: (bitmap: Bitmap) -> Unit = { _ -> },
    backgroundColor: Color = Color.Transparent,
    composable: @Composable () -> Unit,
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

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                ComposeView(context).apply {
                    setContent {
                        Box(
                            modifier = Modifier
                                .background(backgroundColor)
                                .fillMaxSize()
                        ) {
                            composable()
                        }
                    }
                }
            },
            update = { view ->
                view.run {
                    doOnLayout {
                        println("onLayout")
                        val width = view.width
                        val height = view.height
                        println("view width:$width, height:$height")

                        onBitmapped(generateBitmapFromViewWithoutCheckoutIfLaidOut())

//                        if (view.isLaidOut) {
//                            onBitmapped(drawToBitmap())
//                        }
                    }
                }
            }
        )
    }

}

@SuppressLint("UseKtx")
private fun ComposeView.generateBitmapFromViewWithoutCheckoutIfLaidOut(
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap {
    return Bitmap.createBitmap(width, height, config)
        .applyCanvas {
            translate(-scrollX.toFloat(), -scrollY.toFloat())
            draw(this)
        }
}