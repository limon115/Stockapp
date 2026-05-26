// Architected by Khalid Hasan Limon
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainLayoutScreen
import com.example.ui.InventoryViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: InventoryViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(useDarkTheme = isDark) {
                MainLayoutScreen(viewModel = viewModel)
            }
        }
    }
}

