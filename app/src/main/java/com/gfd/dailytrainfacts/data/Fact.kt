package com.gfd.dailytrainfacts.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facts")
data class Fact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val isFavorite: Boolean = false,
    val isShown: Boolean = false,
    val lastShownDate: Long? = null
)
