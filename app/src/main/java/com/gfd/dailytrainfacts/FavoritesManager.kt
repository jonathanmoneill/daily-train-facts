package com.gfd.dailytrainfacts

import android.content.Context
import androidx.core.content.edit

object FavoritesManager {
    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_FAVORITES = "favorite_facts"

    fun toggleFavorite(context: Context, fact: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavorites(context).toMutableSet()
        if (favorites.contains(fact)) {
            favorites.remove(fact)
        } else {
            favorites.add(fact)
        }
        prefs.edit {
            putStringSet(KEY_FAVORITES, favorites)
        }
    }

    fun isFavorite(context: Context, fact: String): Boolean {
        return getFavorites(context).contains(fact)
    }

    fun getFavorites(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }
}
