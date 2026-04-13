package com.gfd.dailytrainfacts.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "facts",
    indices = [Index(value = ["text"], unique = true)]
)
data class Fact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val isFavourite: Boolean = false,
    val isShown: Boolean = false,
    val lastShownDate: Long? = null
)
