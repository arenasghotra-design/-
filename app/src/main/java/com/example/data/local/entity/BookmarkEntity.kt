package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_articles")
data class BookmarkEntity(
    @PrimaryKey
    val articleId: String,
    val title: String,
    val category: String,
    val source: String,
    val savedAt: Long = System.currentTimeMillis()
)
