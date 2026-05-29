package com.cemcakmak.hydrotracker.presentation.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsTopAppBar(scrollBehavior: TopAppBarScrollBehavior) {
    LargeFlexibleTopAppBar(
        title = { Text("Settings") },
        scrollBehavior = scrollBehavior
    )
}
