package com.krllus.bitmappoc

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.krllus.bfc.ComposableBitmapRenderer
import com.krllus.bitmappoc.bucketlist.BucketScreen
import com.krllus.bitmappoc.bucketlist.BucketViewModel
import com.krllus.bitmappoc.bucketlist.ListItemRow
import com.krllus.bitmappoc.bucketlist.data.BucketItem
import com.krllus.bitmappoc.ui.theme.BitmapPOCTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val renderer: ComposableBitmapRenderer by inject()

    private fun requestStoragePermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestStoragePermissionIfNeeded()

        setContent {

            val ctx = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val bucketViewModel: BucketViewModel by viewModels()

            BitmapPOCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        BucketScreen(
                            viewModel = bucketViewModel,
                            onDone = {

                                val items = bucketViewModel.items
                                println("Items to bitmap: ${items.size}")

                                coroutineScope.launch {

//                                    val bitmap = BitmapGenerator(ctx).renderComposeToBitmap(400, 400) {
//                                        ScreenContentToCapture()
//                                    }
//
//                                    bitmap?.let {
//                                        // Save the bitmap to disk or handle it as needed
//                                        // For example, you can save it to a file
//                                        // This is a placeholder for your save logic
//                                        println("Bitmap generated with ${items.size} items.")
//                                        it.saveToDisk(ctx)
//                                    } ?: run {
//                                        println("Bitmap generation failed.")
//                                    }

                                    renderer
                                        .renderComposableToBitmap(canvasSize = Size(384f, 600f)) {
                                            PrintLayoutForItems(items)
                                        }
                                        ?.saveToDisk(ctx)
                                        ?: run {
                                            println("Bitmap generation failed.")
                                        }


//                                    BitmapGenerator(ctx).generate(
//                                        onBitmapped = { bitmap ->
//                                            bitmap?.let {
//                                                // Save the bitmap to disk or handle it as needed
//                                                // For example, you can save it to a file
//                                                // This is a placeholder for your save logic
//                                                println("Bitmap generated with ${items.size} items.")
//                                                coroutineScope.launch { bitmap.saveToDisk(ctx) }
//                                            } ?: run {
//                                                println("Bitmap generation failed.")
//                                            }
//                                        }) {
//                                        PrintLayoutForItems(items)
//                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    fun saveBitmapToDisk(bitmap: Bitmap) {

    }

    companion object {
        private const val REQUEST_WRITE_EXTERNAL_STORAGE = 1001
    }

}

@Composable
fun PrintLayoutForItems(
    items: List<BucketItem>
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items) { item ->
            ListItemRow(item)
            HorizontalDivider()
        }
    }
}