package com.krllus.bitmappoc.bucketlist

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.krllus.bitmappoc.bucketlist.data.BucketItem

class BucketViewModel : ViewModel() {

    private val _items = mutableStateListOf<BucketItem>()
    val items: SnapshotStateList<BucketItem> = _items

    init {
        _items.add(BucketItem("Skydiving", 1200.00))
        _items.add(BucketItem("Visit the Grand Canyon", 500.50))
        _items.add(BucketItem("Learn to play Guitar", 150.75))
        _items.add(BucketItem("Write a Book", 50.00))
        _items.add(BucketItem("Run a Marathon", 200.00))
    }

    fun addItem(name: String, amount: Double) {
        _items.add(BucketItem(name, amount))
    }

}