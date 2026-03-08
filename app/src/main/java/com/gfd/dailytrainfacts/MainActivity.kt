package com.gfd.dailytrainfacts

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.gfd.dailytrainfacts.ui.theme.DailyTrainFactsTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyTrainFactsTheme {
                TrainFactsApp()
            }
        }
    }
}

enum class Screen {
    Home, Fact
}

@Composable
fun TrainFactsApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (currentScreen) {
            Screen.Home -> HomeScreen(onGiveMeFactClicked = { currentScreen = Screen.Fact })
            Screen.Fact -> FactScreen(onExitClicked = { currentScreen = Screen.Home })
        }
    }
}

@Composable
fun HomeScreen(onGiveMeFactClicked: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Full size background image
        Image(
            painter = painterResource(id = R.drawable.train_background_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay to make text readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 135.dp), // Pushes the content higher than center
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAILY TRAIN FACTS",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onGiveMeFactClicked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 135.dp) // Pushes the button higher up from the bottom
                .padding(horizontal = 40.dp)
                .height(64.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
        ) {
            Text(
                text = "Give Me a Train Fact",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Copyright text
        Text(
            text = "© 2026 Fear Dóighiúil Studios",
            color = Color.LightGray.copy(alpha = 1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun FactScreen(onExitClicked: () -> Unit) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(calendar.time)
    val fact = TrainFactsProvider.getFactForToday()
    val context = LocalContext.current

    val (showMenu, setShowMenu) = remember { mutableStateOf(false) }
    val (showOptionsDialog, setShowOptionsDialog) = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Header Image
                Image(
                    painter = painterResource(id = R.drawable.train_silhouette),
                    contentDescription = "Train Illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 24.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(40.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = fact,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                            lineHeight = 36.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Standard Android Sharing Button
                FilledTonalButton(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Did you know? $fact #DailyTrainFacts")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Share this Fact")
                }

                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Come back tomorrow for a new train fact...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onExitClicked,
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Exit", fontSize = 18.sp)
            }
        }

        // Options Menu Icon
        IconButton(
            onClick = { setShowMenu(true) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { setShowMenu(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Reminder Settings") },
                    onClick = {
                        setShowMenu(false)
                        setShowOptionsDialog(true)
                    },
                    leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
            }
        }
    }

    if (showOptionsDialog) {
        ReminderOptionsDialog(onDismiss = { setShowOptionsDialog(false) })
    }
}

@Composable
fun ReminderOptionsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val (isEnabled, setIsEnabled) = remember { mutableStateOf(ReminderManager.isReminderEnabled(context)) }

    val initialTime = remember { ReminderManager.getReminderTime(context) }
    var selectedHour by remember { mutableIntStateOf(initialTime.first) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.second) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ReminderManager.setReminder(context, true, selectedHour, selectedMinute)
            setIsEnabled(true)
        } else {
            Toast.makeText(context, "Notification permission required for reminders", Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Reminder") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Reminders", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                        ReminderManager.setReminder(context, true, selectedHour, selectedMinute)
                                        setIsEnabled(true)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    ReminderManager.setReminder(context, true, selectedHour, selectedMinute)
                                    setIsEnabled(true)
                                }
                            } else {
                                ReminderManager.setReminder(context, false, selectedHour, selectedMinute)
                                setIsEnabled(false)
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                selectedHour = h
                                selectedMinute = m
                                if (isEnabled) {
                                    ReminderManager.setReminder(context, true, h, m)
                                }
                            },
                            selectedHour,
                            selectedMinute,
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reminder Time: ${String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
