package com.example.jetpack.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun JetpackComposeTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}