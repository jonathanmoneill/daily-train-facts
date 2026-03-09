package com.gfd.dailytrainfacts

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gfd.dailytrainfacts.data.Fact
import com.gfd.dailytrainfacts.ui.theme.DailyTrainFactsTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyTrainFactsTheme {
                val app = LocalContext.current.applicationContext as DailyTrainFactsApplication
                val viewModel: TrainFactsViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            TrainFactsViewModel(app.repository)
                        }
                    }
                )
                val context = LocalContext.current
                
                LaunchedEffect(Unit) {
                    viewModel.init(context)
                }
                
                TrainFactsApp(viewModel)
            }
        }
    }
}

enum class Screen {
    Home, Fact, Favorites
}

@Composable
fun TrainFactsApp(viewModel: TrainFactsViewModel) {
    val currentScreen by viewModel.currentScreen

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                Screen.Home -> HomeScreen(onGiveMeFactClicked = { viewModel.navigateTo(Screen.Fact) })
                Screen.Fact -> FactScreen(
                    onExitClicked = { viewModel.navigateTo(Screen.Home) },
                    onNavigateToFavorites = { viewModel.navigateTo(Screen.Favorites) },
                    viewModel = viewModel
                )
                Screen.Favorites -> FavoritesScreen(
                    onBackClicked = { viewModel.navigateTo(Screen.Fact) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun HomeScreen(onGiveMeFactClicked: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.train_background_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 135.dp),
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
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGiveMeFactClicked()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 135.dp)
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

        Text(
            text = "© 2026 Fear Dóighiúil Studios",
            color = Color.LightGray.copy(alpha = 1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )
    }
}

@Composable
fun FactScreen(
    onExitClicked: () -> Unit, 
    onNavigateToFavorites: () -> Unit,
    viewModel: TrainFactsViewModel
) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(calendar.time)
    
    val currentFact by viewModel.currentFact.collectAsState()
    
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

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

            val scrollState = rememberScrollState()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.train_silhouette),
                    contentDescription = "Train Illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 24.dp),
                    contentScale = ContentScale.Crop
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        SelectionContainer {
                            Text(
                                text = currentFact?.text ?: "Loading...",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp),
                                lineHeight = 36.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isFavorite = currentFact?.isFavorite ?: false
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentFact?.let { viewModel.toggleFavorite(it) }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = currentFact != null
                    ) {
                        Text(if (isFavorite) "In Favorites" else "Add to Favorites", fontSize = 12.sp)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentFact?.let {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Did you know? ${it.text} #DailyTrainFacts")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = currentFact != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Share Fact", fontSize = 12.sp)
                    }
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
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onExitClicked()
                },
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 48.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Exit", fontSize = 18.sp)
            }
        }

        IconButton(
            onClick = { setShowMenu(true) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { setShowMenu(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Favorite Facts") },
                    onClick = {
                        setShowMenu(false)
                        onNavigateToFavorites()
                    },
                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                )
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
        ReminderOptionsDialog(
            onDismiss = { setShowOptionsDialog(false) },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderOptionsDialog(onDismiss: () -> Unit, viewModel: TrainFactsViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val isEnabled by viewModel.isReminderEnabled
    val time by viewModel.reminderTime
    val isPermissionGranted by viewModel.isNotificationPermissionGranted

    val (showTimePicker, setShowTimePicker) = remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationPermissionGranted(isGranted)
        if (isGranted) {
            viewModel.toggleReminder(context, true)
        } else {
            Toast.makeText(context, "Notification permission required for reminders", Toast.LENGTH_LONG).show()
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.first,
            initialMinute = time.second,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { setShowTimePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.updateReminderTime(context, timePickerState.hour, timePickerState.minute)
                        setShowTimePicker(false)
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { setShowTimePicker(false) }
                ) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
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
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPermissionGranted) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleReminder(context, true)
                                }
                            } else {
                                viewModel.toggleReminder(context, false)
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        setShowTimePicker(true)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reminder Time: ${String.format(Locale.getDefault(), "%02d:%02d", time.first, time.second)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) {
                Text("Done")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onBackClicked: () -> Unit, viewModel: TrainFactsViewModel) {
    val haptic = LocalHapticFeedback.current
    val favorites by viewModel.favoriteFacts.collectAsState()
    
    val (factToRemove, setFactToRemove) = remember { mutableStateOf<Fact?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Favorite Facts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackClicked()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No favorites yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { fact ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fact.text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                setFactToRemove(fact)
                            }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Remove from favorites", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for removal
    if (factToRemove != null) {
        AlertDialog(
            onDismissRequest = { setFactToRemove(null) },
            title = { Text("Remove from Favorites?") },
            text = { Text("Are you sure you want to remove this fact from your favorites list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        factToRemove.let { fact ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleFavorite(fact)
                        }
                        setFactToRemove(null)
                    }
                ) {
                    Text("Remove", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { setFactToRemove(null) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}
