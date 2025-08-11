package com.krllus.bitmappoc

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
import androidx.compose.ui.platform.LocalContext
import com.krllus.bfc.BitmapGenerator
import com.krllus.bitmappoc.bucketlist.BucketScreen
import com.krllus.bitmappoc.bucketlist.BucketViewModel
import com.krllus.bitmappoc.bucketlist.ListItemRow
import com.krllus.bitmappoc.bucketlist.data.BucketItem
import com.krllus.bitmappoc.ui.theme.BitmapPOCTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                BitmapGenerator(context = ctx).generate(
                                    onBitmapped = { bitmap ->
                                        coroutineScope.launch {
                                            bitmap?.let {
                                                // Save the bitmap to disk or handle it as needed
                                                // For example, you can save it to a file
                                                // This is a placeholder for your save logic
                                                println("Bitmap generated with ${items.size} items.")
                                                it.saveToDisk(ctx)
                                            } ?: run {
                                                println("Bitmap generation failed.")
                                            }
                                        }
                                    }) {
                                    PrintLayoutForItems(items)
                                }
                            }
                        )
                    }
                }
            }
        }
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