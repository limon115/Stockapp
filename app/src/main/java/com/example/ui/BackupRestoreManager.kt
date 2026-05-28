package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.example.data.AppDatabase
import com.example.data.AuditLogEntry
import com.example.data.InventoryItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreManager {

    val polwelBaseDir: File
        get() = File(Environment.getExternalStorageDirectory(), "polwel")
    val reportsDir: File
        get() = File(polwelBaseDir, "reports")
    val backupsDir: File
        get() = File(polwelBaseDir, "backups")

    fun hasStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        } else {
            return context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(java.lang.String.format("package:%s", context.applicationContext.packageName))
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                context.startActivity(intent)
            }
        } else {
            // Permission should be requested in the activity
        }
    }

    fun initializeDirectories() {
        if (!polwelBaseDir.exists()) polwelBaseDir.mkdirs()
        if (!reportsDir.exists()) reportsDir.mkdirs()
        if (!backupsDir.exists()) backupsDir.mkdirs()
    }

    suspend fun createJsonBackup(context: Context): String? {
        if (!hasStoragePermission(context)) return null
        initializeDirectories()

        try {
            val dao = AppDatabase.getDatabase(context).inventoryDao()
            val items = dao.getAllInventoryList()
            val logs = dao.getAllAuditLogsList()

            val rootObj = JSONObject()

            val itemsArray = JSONArray()
            for (item in items) {
                val obj = JSONObject()
                obj.put("itemId", item.itemId)
                obj.put("name", item.name)
                obj.put("sku", item.sku)
                obj.put("currentStock", item.currentStock)
                obj.put("category", item.category)
                obj.put("cost", item.cost)
                obj.put("lowStockThreshold", item.lowStockThreshold)
                itemsArray.put(obj)
            }
            rootObj.put("inventory", itemsArray)

            val logsArray = JSONArray()
            for (log in logs) {
                val obj = JSONObject()
                obj.put("logId", log.logId)
                obj.put("itemId", log.itemId)
                obj.put("productName", log.productName)
                obj.put("transactionType", log.transactionType)
                obj.put("quantityChanged", log.quantityChanged)
                obj.put("stockValue", log.stockValue)
                obj.put("details", log.details)
                obj.put("timestamp", log.timestamp)
                logsArray.put(obj)
            }
            rootObj.put("auditLogs", logsArray)

            val jsonString = rootObj.toString(4) // Pretty print

            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = sdf.format(Date())
            val backupFile = File(backupsDir, "backup_$timestamp.json")

            val out = FileOutputStream(backupFile)
            out.write(jsonString.toByteArray(Charsets.UTF_8))
            out.close()

            return backupFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun restoreJsonBackup(context: Context, backupFile: File): Boolean {
        if (!hasStoragePermission(context)) return false

        try {
            val inStream = FileInputStream(backupFile)
            val jsonString = inStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val rootObj = JSONObject(jsonString)

            val itemsArray = rootObj.getJSONArray("inventory")
            val items = mutableListOf<InventoryItem>()
            for (i in 0 until itemsArray.length()) {
                val obj = itemsArray.getJSONObject(i)
                items.add(
                    InventoryItem(
                        itemId = obj.optLong("itemId", 0),
                        name = obj.getString("name"),
                        sku = obj.getString("sku"),
                        currentStock = obj.getInt("currentStock"),
                        category = obj.getString("category"),
                        cost = obj.getDouble("cost"),
                        lowStockThreshold = obj.getInt("lowStockThreshold")
                    )
                )
            }

            val logsArray = rootObj.getJSONArray("auditLogs")
            val logs = mutableListOf<AuditLogEntry>()
            for (i in 0 until logsArray.length()) {
                val obj = logsArray.getJSONObject(i)
                logs.add(
                    AuditLogEntry(
                        logId = obj.optLong("logId", 0),
                        itemId = obj.getLong("itemId"),
                        productName = obj.getString("productName"),
                        transactionType = obj.getString("transactionType"),
                        quantityChanged = obj.getInt("quantityChanged"),
                        stockValue = obj.getDouble("stockValue"),
                        details = obj.optString("details", ""),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }

            val dao = AppDatabase.getDatabase(context).inventoryDao()
            dao.clearInventory()
            dao.clearAuditLogs()

            for (item in items) {
                dao.insertItem(item)
            }

            for (log in logs) {
                dao.insertAuditLog(log)
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getAvailableBackups(): List<File> {
        if (!backupsDir.exists()) return emptyList()
        return backupsDir.listFiles { file -> file.isFile && file.name.startsWith("backup_") && file.name.endsWith(".json") }
            ?.sortedByDescending { it.name }
            ?: emptyList()
    }
}
