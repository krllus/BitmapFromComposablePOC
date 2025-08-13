package com.krllus.bfc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.core.graphics.createBitmap

class BitmapGenerator(private val context: Context) {

    fun generate(
        onBitmapped: (bitmap: Bitmap?) -> Unit,
        composable: @Composable () -> Unit,
    ) {
        ComposeView(context).apply {
            setContent {
                BitmapFromComposable(
                    onBitmapped = onBitmapped,
                    composable = composable
                )
            }
        }

    }


    fun renderComposeToBitmap(
        width: Int,
        height: Int,
        content: @Composable () -> Unit
    ): Bitmap {
        return runBlocking {
            val composeView = ComposeView(context)

            val recomposer = Recomposer(coroutineContext)
            composeView.compositionContext = recomposer

            val recomposerJob = launch { recomposer.runRecomposeAndApplyChanges() }

            try {
                composeView.setContent {
                    content()
                }

                recomposer.awaitIdle()

                composeView.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
                )
                composeView.layout(0, 0, width, height)

                val bitmap = createBitmap(width, height)
                val canvas = Canvas(bitmap)
                composeView.draw(canvas)

                bitmap
            } finally {
                recomposer.close()
                recomposerJob.cancel()
            }
        }
    }

}