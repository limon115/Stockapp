// Architected by Khalid Hasan Limon
package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY itemId DESC")
    fun getAllInventory(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE itemId = :id LIMIT 1")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory WHERE sku = :sku LIMIT 1")
    suspend fun getItemBySku(sku: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("SELECT * FROM audit_log ORDER BY logId DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(entry: AuditLogEntry): Long
}
