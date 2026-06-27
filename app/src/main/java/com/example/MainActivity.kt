package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.PhotoRepository
import com.example.ui.EditorScreen
import com.example.ui.PhotoEditorViewModel
import com.example.ui.PhotoEditorViewModelFactory
import com.example.ui.StudioScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PhotoRepository(database.photoDao())
        
        setContent {
            MyApplicationTheme {
                val viewModel: PhotoEditorViewModel = viewModel(
                    factory = PhotoEditorViewModelFactory(application, repository)
                )
                
                // Simple state-based navigation for 100% build reliability and speed
                var currentScreen by remember { mutableStateOf("Studio") }
                
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (currentScreen) {
                        "Studio" -> {
                            StudioScreen(
                                viewModel = viewModel,
                                onNavigateToEditor = { currentScreen = "Editor" }
                            )
                        }
                        "Editor" -> {
                            EditorScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "Studio" }
                            )
                        }
                    }
                }
            }
        }
    }
}
