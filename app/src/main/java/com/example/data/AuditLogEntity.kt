// Architected by Khalid Hasan Limon
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_log")
data class AuditLogEntry(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val itemId: Long,
    val sku: String,
    val transactionType: String, // "IN" or "OUT"
    val quantityChanged: Int,
    val timestamp: Long
)
