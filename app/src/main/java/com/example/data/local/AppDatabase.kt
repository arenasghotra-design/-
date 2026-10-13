package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.entity.AlertHistoryEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.SubscriptionKeywordEntity

@Database(
    entities = [
        SubscriptionKeywordEntity::class,
        AlertHistoryEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cosmo_frontier.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
