package com.gfd.dailytrainfacts.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Fact::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun factDao(): FactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns while preserving existing data
                db.execSQL("ALTER TABLE facts ADD COLUMN isShown INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE facts ADD COLUMN lastShownDate INTEGER")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "train_facts_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
