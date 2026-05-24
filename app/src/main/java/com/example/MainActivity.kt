package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.LauncherRepository
import com.example.ui.LauncherViewModel
import com.example.ui.LauncherViewModelFactory
import com.example.ui.SafeLauncherRootUi
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: LauncherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = LauncherRepository(database)
        val factory = LauncherViewModelFactory(this.applicationContext, repository)
        viewModel = ViewModelProvider(this, factory)[LauncherViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SafeLauncherRootUi(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.onHomeResumed()
            viewModel.refreshAppsList()
            viewModel.refreshPinState()
        }
    }
}
