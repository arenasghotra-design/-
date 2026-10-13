package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription_keywords")
data class SubscriptionKeywordEntity(
    @PrimaryKey
    val keyword: String,
    val createdAt: Long = System.currentTimeMillis(),
    val matchCount: Int = 0,
    val isEnabled: Boolean = true
)
