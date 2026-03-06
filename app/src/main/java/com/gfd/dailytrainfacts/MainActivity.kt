package com.gfd.dailytrainfacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2C3E50))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚂",
                    fontSize = 120.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DAILY TRAIN FACTS",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Discover the world of rails",
                    color = Color.LightGray,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = onGiveMeFactClicked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .height(64.dp)
                .width(280.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
        ) {
            Text(text = "Give Me a Train Fact", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FactScreen(onExitClicked: () -> Unit) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(calendar.time)
    val fact = TrainFactsProvider.getFactForToday()

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
                Text(
                    text = fact,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                    lineHeight = 36.sp
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Come back tomorrow for more facts",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }

        Button(
            onClick = onExitClicked,
            modifier = Modifier
                .padding(bottom = 48.dp)
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Exit", fontSize = 18.sp)
        }
    }
}
