package com.krllus.bfc

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

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
}