package com.gfd.dailytrainfacts

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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                // Re-check permissions when app returns to foreground
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.checkNotificationPermission(context)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.init(context)
                }
                
                TrainFactsApp(viewModel)
            }
        }
    }
}

enum class Screen {
    Home, Fact, Favourites
}

@Composable
fun TrainFactsApp(viewModel: TrainFactsViewModel) {
    val currentScreen by viewModel.currentScreen

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = currentScreen == Screen.Favourites,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { isFavourites ->
            if (isFavourites) {
                FavouritesScreen(
                    onBackClicked = { viewModel.navigateTo(Screen.Home) },
                    viewModel = viewModel
                )
            } else {
                HomeScreen(
                    onGiveMeFactClicked = { viewModel.navigateTo(Screen.Fact) },
                    onNavigateToFavourites = { viewModel.navigateTo(Screen.Favourites) },
                    viewModel = viewModel,
                    showFactOverlay = currentScreen == Screen.Fact,
                    onCloseFactOverlay = { viewModel.navigateTo(Screen.Home) }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onGiveMeFactClicked: () -> Unit,
    onNavigateToFavourites: () -> Unit,
    viewModel: TrainFactsViewModel,
    showFactOverlay: Boolean = false,
    onCloseFactOverlay: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val (showMenu, setShowMenu) = remember { mutableStateOf(false) }
    val (showOptionsDialog, setShowOptionsDialog) = remember { mutableStateOf(false) }
    
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
                .background(Color.Black.copy(alpha = 0.25f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(bottom = 145.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAILY TRAIN FACTS",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    ),
                    letterSpacing = 4.sp
                )
            )
        }

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGiveMeFactClicked()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
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
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )

        IconButton(
            onClick = { setShowMenu(true) },
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { setShowMenu(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Favourite Facts") },
                    onClick = {
                        setShowMenu(false)
                        onNavigateToFavourites()
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

        if (showFactOverlay) {
            val currentFact by viewModel.currentFact.collectAsState()
            FactOverlay(
                fact = currentFact,
                onCloseClicked = onCloseFactOverlay,
                viewModel = viewModel
            )
        }
    }

    if (showOptionsDialog) {
        ReminderOptionsDialog(
            onDismiss = { setShowOptionsDialog(false) },
            viewModel = viewModel
        )
    }
}

@Composable
fun FactOverlay(
    fact: Fact?,
    onCloseClicked: () -> Unit, 
    viewModel: TrainFactsViewModel
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(calendar.time)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onCloseClicked),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .animateContentSize()
                .clickable(enabled = false) {}, 
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header section
                Image(
                    painter = painterResource(id = R.drawable.train_silhouette),
                    contentDescription = "Train Illustration",
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Fact area - wraps height naturally, scrollable only if it exceeds max limit
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp) 
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SelectionContainer {
                        Text(
                            text = fact?.text ?: "Loading today's train fact...",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            lineHeight = 34.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // Action Buttons at the bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isFavourite = fact?.isFavourite ?: false
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            fact?.let { viewModel.toggleFavourite(it) }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        enabled = fact != null
                    ) {
                        Text(if (isFavourite) "Favourite" else "Add to Favourites", fontSize = 11.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isFavourite) Color.Red else LocalContentColor.current
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            fact?.let {
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
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        enabled = fact != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCloseClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Close", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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
                                if (Build.VERSION.SDK_INT >= 33 && !isPermissionGranted) {
                                    permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
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
fun FavouritesScreen(onBackClicked: () -> Unit, viewModel: TrainFactsViewModel) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val favourites by viewModel.favouriteFacts.collectAsState()
    val selectedFact by viewModel.selectedFavouriteFact
    
    val (factToRemove, setFactToRemove) = remember { mutableStateOf<Fact?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Favourite Facts", fontWeight = FontWeight.Bold) },
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
            if (favourites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No favourites yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favourites) { fact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.selectFavouriteFact(fact)
                                },
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
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Did you know? ${fact.text} #DailyTrainFacts")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share fact")
                                }
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    setFactToRemove(fact)
                                }) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Remove from favourites", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedFact != null) {
            FactOverlay(
                fact = selectedFact,
                onCloseClicked = { viewModel.selectFavouriteFact(null) },
                viewModel = viewModel
            )
        }
    }

    // Confirmation Dialog for removal
    if (factToRemove != null) {
        AlertDialog(
            onDismissRequest = { setFactToRemove(null) },
            title = { Text("Remove from Favourites?") },
            text = { Text("Are you sure you want to remove this fact from your favourites list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        factToRemove.let { fact ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleFavourite(fact)
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
