package de.arturo.bartab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.arturo.bartab.ui.BarTabApp
import de.arturo.bartab.ui.theme.BarTabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BarTabTheme {
                BarTabApp()
            }
        }
    }
}
