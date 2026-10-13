package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AlertHistoryEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.SubscriptionKeywordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    // Keywords
    @Query("SELECT * FROM subscription_keywords ORDER BY createdAt DESC")
    fun getAllKeywords(): Flow<List<SubscriptionKeywordEntity>>

    @Query("SELECT * FROM subscription_keywords WHERE isEnabled = 1")
    suspend fun getActiveKeywords(): List<SubscriptionKeywordEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertKeyword(keyword: SubscriptionKeywordEntity)

    @Update
    suspend fun updateKeyword(keyword: SubscriptionKeywordEntity)

    @Query("UPDATE subscription_keywords SET isEnabled = :isEnabled WHERE keyword = :keyword")
    suspend fun setKeywordEnabled(keyword: String, isEnabled: Boolean)

    @Query("UPDATE subscription_keywords SET matchCount = :matchCount WHERE keyword = :keyword")
    suspend fun updateMatchCount(keyword: String, matchCount: Int)

    @Query("DELETE FROM subscription_keywords WHERE keyword = :keyword")
    suspend fun deleteKeyword(keyword: String)

    // Alerts
    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertHistoryEntity): Long

    @Query("UPDATE alert_history SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: Long)

    @Query("UPDATE alert_history SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM alert_history")
    suspend fun clearAlertHistory()

    // Bookmarks
    @Query("SELECT * FROM bookmarked_articles ORDER BY savedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarked_articles WHERE articleId = :articleId")
    suspend fun deleteBookmark(articleId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_articles WHERE articleId = :articleId)")
    fun isBookmarked(articleId: String): Flow<Boolean>
}
