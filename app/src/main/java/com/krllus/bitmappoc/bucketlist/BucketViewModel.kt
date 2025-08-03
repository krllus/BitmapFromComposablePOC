package com.krllus.bitmappoc.bucketlist

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.krllus.bitmappoc.bucketlist.data.BucketItem

class BucketViewModel : ViewModel() {

    private val _items = mutableStateListOf<BucketItem>()
    val items: SnapshotStateList<BucketItem> = _items

    fun addItem(name: String, amount: Double) {
        _items.add(BucketItem(name, amount))
    }

}