package com.krllus.bitmappoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.krllus.bitmappoc.ui.theme.BitmapPOCTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val ctx = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            BitmapPOCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

//                        Something()

//                        BitmapFromComposableFullSnippet()

                        BitmapComposable(
                            onBitmapped = { bitmap ->
                                coroutineScope.launch {
                                    bitmap?.saveToDisk(ctx)
                                    println("bitmap saved to disk")
                                }
                            },
                            backgroundColor = Color.Red,
                        ) {
                            ScreenContentToCapture()
                        }
                    }
                }
            }
        }
    }
}