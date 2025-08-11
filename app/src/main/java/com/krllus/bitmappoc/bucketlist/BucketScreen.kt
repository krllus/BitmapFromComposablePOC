package com.krllus.bitmappoc.bucketlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.krllus.bitmappoc.bucketlist.data.BucketItem

@Composable
fun BucketScreen(
    viewModel: BucketViewModel,
    onDone: () -> Unit = {}
) {

    var nameInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    val items = viewModel.items

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input fields
        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    viewModel.addItem(nameInput, amount)
                    // Clear inputs after adding
                    nameInput = ""
                    amountInput = ""
                },
            ) {
                Text("Add Item")
            }

            Button(
                onClick = onDone,
            ) {
                Text("Done")
            }

        }



        Spacer(modifier = Modifier.height(16.dp))

        // List of items
        if (items.isEmpty()) {
            Text(
                text = "No items added yet.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items) { item ->
                    ListItemRow(item)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ListItemRow(
    item: BucketItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
        Text(text = "$${"%.2f".format(item.amount)}", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun BucketScreenPreview() {
    val previewViewModel = BucketViewModel().apply {
        addItem("Groceries", 75.50)
        addItem("Gas", 40.00)
    }
    BucketScreen(previewViewModel)
}
