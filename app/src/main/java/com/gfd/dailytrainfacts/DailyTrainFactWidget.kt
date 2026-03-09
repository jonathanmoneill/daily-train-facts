package com.gfd.dailytrainfacts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.util.Calendar

class DailyTrainFactWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as DailyTrainFactsApplication
        val repository = app.repository
        val count = repository.getFactCount()
        
        val factText = if (count > 0) {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = now
            val localTimeInMillis = now + calendar.timeZone.getOffset(now)
            val daysSinceEpoch = localTimeInMillis / (24 * 60 * 60 * 1000)
            val index = (daysSinceEpoch % count).toInt()
            repository.getFactAtIndex(index)?.text ?: TrainFactsProvider.getFactForToday()
        } else {
            TrainFactsProvider.getFactForToday()
        }

        provideContent {
            GlanceTheme {
                WidgetContent(factText)
            }
        }
    }

    @Composable
    private fun WidgetContent(fact: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Daily Train Fact",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GlanceTheme.colors.primary
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fact,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onSurface
                    ),
                    modifier = GlanceModifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
