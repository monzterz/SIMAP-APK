package com.arffsnia.simap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arffsnia.simap.ui.AppRoot
import com.arffsnia.simap.ui.theme.SimapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimapTheme {
                AppRoot()
            }
        }
    }
}