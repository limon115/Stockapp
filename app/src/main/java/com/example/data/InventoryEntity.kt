// Architected by Khalid Hasan Limon
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val name: String,
    val sku: String,
    val currentStock: Int,
    val category: String,
    val cost: Double,
    val lowStockThreshold: Int = 2
)
