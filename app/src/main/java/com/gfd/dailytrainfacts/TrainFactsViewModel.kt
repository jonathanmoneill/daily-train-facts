package com.gfd.dailytrainfacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gfd.dailytrainfacts.data.Fact
import com.gfd.dailytrainfacts.data.FactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainFactsViewModel(private val repository: FactRepository) : ViewModel() {
    private val _currentScreen = mutableStateOf(Screen.Home)
    val currentScreen: State<Screen> = _currentScreen

    private val _isReminderEnabled = mutableStateOf(false)
    val isReminderEnabled: State<Boolean> = _isReminderEnabled

    private val _reminderTime = mutableStateOf(Pair(9, 0))
    val reminderTime: State<Pair<Int, Int>> = _reminderTime

    private val _isNotificationPermissionGranted = mutableStateOf(false)
    val isNotificationPermissionGranted: State<Boolean> = _isNotificationPermissionGranted

    private val _currentFact = MutableStateFlow<Fact?>(null)
    val currentFact: StateFlow<Fact?> = _currentFact.asStateFlow()

    private val _favouriteFacts = MutableStateFlow<List<Fact>>(emptyList())
    val favouriteFacts: StateFlow<List<Fact>> = _favouriteFacts.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun init(context: Context) {
        _isReminderEnabled.value = ReminderManager.isReminderEnabled(context)
        _reminderTime.value = ReminderManager.getReminderTime(context)
        checkNotificationPermission(context)
        
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
            loadTodayFact()
            repository.getFavouriteFacts().collect {
                _favouriteFacts.value = it
            }
        }
    }

    private suspend fun loadTodayFact() {
        _currentFact.value = repository.getTodayFact()
    }

    fun toggleFavourite(fact: Fact) {
        viewModelScope.launch {
            repository.toggleFavourite(fact.text)
            // Reload current fact to update its favourite status in UI
            _currentFact.value = repository.getFactByText(fact.text)
        }
    }

    fun toggleReminder(context: Context, enabled: Boolean) {
        val (hour, minute) = _reminderTime.value
        ReminderManager.setReminder(context, enabled, hour, minute)
        _isReminderEnabled.value = enabled
    }

    fun updateReminderTime(context: Context, hour: Int, minute: Int) {
        _reminderTime.value = Pair(hour, minute)
        if (_isReminderEnabled.value) {
            ReminderManager.setReminder(context, true, hour, minute)
        }
    }

    fun checkNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _isNotificationPermissionGranted.value = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            _isNotificationPermissionGranted.value = true
        }
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        _isNotificationPermissionGranted.value = granted
    }
}
