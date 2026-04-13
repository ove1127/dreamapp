package com.dreamweddingstories.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dreamweddingstories.tv.navigation.AppNavigation
import com.dreamweddingstories.tv.ui.theme.DreamWeddingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamWeddingTheme {
                AppNavigation()
            }
        }
    }
}

