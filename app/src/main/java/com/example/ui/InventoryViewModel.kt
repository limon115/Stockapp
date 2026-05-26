// Architected by Khalid Hasan Limon
package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    
    val inventoryState: StateFlow<List<InventoryItem>>
    val auditLogsState: StateFlow<List<AuditLogEntry>>
    
    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.inventoryDao()
        repository = InventoryRepository(dao)

        inventoryState = repository.allInventory
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        auditLogsState = repository.allAuditLogs
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addItem(name: String, sku: String, initialStock: Int, category: String, cost: Double, lowStockThreshold: Int = 2, onCompleted: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getItemBySku(sku)
            if (existing != null) {
                // If item matches SKU, adjust its stock instead
                repository.adjustStock(existing.itemId, initialStock, "IN")
                launch(Dispatchers.Main) { onCompleted(existing.itemId) }
            } else {
                val item = InventoryItem(
                    name = name,
                    sku = sku,
                    currentStock = initialStock,
                    category = category,
                    cost = cost,
                    lowStockThreshold = lowStockThreshold
                )
                val newId = repository.insertItem(item)
                
                // Track initial stock in audit log
                if (initialStock > 0) {
                    val logEntry = AuditLogEntry(
                        itemId = newId,
                        sku = sku,
                        transactionType = "IN",
                        quantityChanged = initialStock,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertAuditLog(logEntry)
                }
                launch(Dispatchers.Main) { onCompleted(newId) }
            }
        }
    }

    fun updateLowStockThreshold(itemId: Long, threshold: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = repository.getItemById(itemId)
            if (item != null) {
                val updated = item.copy(lowStockThreshold = threshold)
                repository.updateItem(updated)
            }
        }
    }

    fun adjustStock(itemId: Long, quantityChanged: Int, transactionType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adjustStock(itemId, quantityChanged, transactionType)
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(item)
        }
    }

    fun findItemBySku(sku: String, onFound: (InventoryItem?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = repository.getItemBySku(sku)
            launch(Dispatchers.Main) { onFound(item) }
        }
    }

    fun triggerCsvExport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = inventoryState.value
            val logs = auditLogsState.value
            val result = ExportEngine.exportToCSV(context, items, logs)
            _exportMessage.value = if (result.startsWith("content://") || result.startsWith("file://")) {
                "CSV Exported successfully to Downloads!"
            } else {
                "Export Error: $result"
            }
        }
    }

    fun triggerPdfExport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = inventoryState.value
            val result = ExportEngine.exportToPDF(context, items)
            _exportMessage.value = if (result.startsWith("content://") || result.startsWith("file://")) {
                "PDF Report exported successfully to Downloads!"
            } else {
                "Export Error: $result"
            }
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}
