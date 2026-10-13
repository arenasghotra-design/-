package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpaceArticle
import com.example.ui.components.GlowingBorderCard
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PropulsionOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SpaceUiState

@Composable
fun BookmarksScreen(
    state: SpaceUiState,
    onArticleClick: (SpaceArticle) -> Unit,
    onToggleBookmark: (SpaceArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmarkedArticles = state.articles.filter { state.bookmarkedIds.contains(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "本地收藏数据库 (${bookmarkedArticles.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "支持离线浏览",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (bookmarkedArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "暂无收藏的航天动态",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "浏览前沿资讯时点击书签图标即可离线收藏",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookmarkedArticles, key = { it.id }) { article ->
                    GlowingBorderCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bookmark_item_${article.id}"),
                        onClick = { onArticleClick(article) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = article.source,
                                    fontSize = 11.sp,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = article.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = article.formattedDate,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            IconButton(
                                onClick = { onToggleBookmark(article) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkRemove,
                                    contentDescription = "取消收藏",
                                    tint = PropulsionOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
