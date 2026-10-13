package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_history")
data class AlertHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchedKeyword: String,
    val articleId: String,
    val articleTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
