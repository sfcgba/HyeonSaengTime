package com.example.hyeonsaengtime

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.hyeonsaengtime.ui.theme.HyeonSaengTimeTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HyeonSaengSettingsStore(this).cleanupLegacySettings()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        val intent = Intent(this, TrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        setContent {
            HyeonSaengTimeTheme {
                val context = LocalContext.current
                val localStore = remember(context) { HyeonSaengLocalStore(context) }
                val roomStore = remember(context) { RoomLocalStore(context) }
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var pendingDailyRecap by remember { mutableStateOf<DayResult?>(null) }
                var pendingRoomResult by remember {
                    mutableStateOf<RoomOverviewResult>(RoomOverviewResult.NotCreated)
                }

                LaunchedEffect(localStore, roomStore, pendingDailyRecap) {
                    while (true) {
                        if (pendingDailyRecap == null) {
                            val recap = localStore.getPendingDailyRecap()
                            if (recap != null) {
                                pendingDailyRecap = recap
                                pendingRoomResult = roomStore.getRoomOverview()
                            }
                        }
                        delay(1000L)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(
                            onCreateRoomClick = { currentScreen = AppScreen.ROOM },
                            onSettingsClick = { currentScreen = AppScreen.SETTINGS },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.ROOM -> RoomScreen(
                            onBack = { currentScreen = AppScreen.HOME },
                            onCreated = { currentScreen = AppScreen.HOME },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            onBack = { currentScreen = AppScreen.HOME },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                pendingDailyRecap?.let { recap ->
                    DailyRecapDialog(
                        dayResult = recap,
                        roomResult = pendingRoomResult,
                        onDismiss = {
                            localStore.markDailyRecapShown()
                            pendingDailyRecap = null
                        }
                    )
                }
            }
        }
    }
}

private enum class AppScreen {
    HOME,
    ROOM,
    SETTINGS
}
