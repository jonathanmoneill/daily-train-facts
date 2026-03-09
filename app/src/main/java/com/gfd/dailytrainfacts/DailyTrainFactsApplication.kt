package com.gfd.dailytrainfacts

import android.app.Application
import com.gfd.dailytrainfacts.data.AppDatabase
import com.gfd.dailytrainfacts.data.FactRepository

class DailyTrainFactsApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { FactRepository(database.factDao()) }
}
