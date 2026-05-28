// Architected by Khalid Hasan Limon
package com.example.data

import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {

    val allInventory: Flow<List<InventoryItem>> = inventoryDao.getAllInventory()
    val allAuditLogs: Flow<List<AuditLogEntry>> = inventoryDao.getAllAuditLogs()

    suspend fun getItemById(id: Long): InventoryItem? = inventoryDao.getItemById(id)

    suspend fun getItemBySku(sku: String): InventoryItem? = inventoryDao.getItemBySku(sku)

    suspend fun insertItem(item: InventoryItem): Long = inventoryDao.insertItem(item)

    suspend fun updateItem(item: InventoryItem) = inventoryDao.updateItem(item)

    suspend fun deleteItem(item: InventoryItem) = inventoryDao.deleteItem(item)

    suspend fun insertAuditLog(entry: AuditLogEntry): Long = inventoryDao.insertAuditLog(entry)

    suspend fun adjustStock(itemId: Long, quantityChanged: Int, transactionType: String, details: String = "Stock adjustment", overrideValue: Double? = null): Boolean {
        val item = inventoryDao.getItemById(itemId) ?: return false
        val newStock = if (transactionType == "IN") {
            item.currentStock + quantityChanged
        } else {
            maxOf(0, item.currentStock - quantityChanged)
        }
        
        val updatedItem = item.copy(currentStock = newStock)
        inventoryDao.updateItem(updatedItem)
        
        val logEntry = AuditLogEntry(
            itemId = itemId,
            productName = item.name,
            transactionType = transactionType,
            quantityChanged = quantityChanged,
            stockValue = overrideValue ?: (item.cost * quantityChanged),
            details = details,
            timestamp = System.currentTimeMillis()
        )
        inventoryDao.insertAuditLog(logEntry)
        return true
    }
}
