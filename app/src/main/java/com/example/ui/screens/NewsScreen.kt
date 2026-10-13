package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpaceArticle
import com.example.data.model.SpaceCategory
import com.example.ui.components.CosmicImpactBadge
import com.example.ui.components.GlowingBorderCard
import com.example.ui.components.PulsingRadarBeacon
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LaserRed
import com.example.ui.theme.OrbitBlue
import com.example.ui.theme.PropulsionOrange
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceBorderHighlight
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceElevated
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SpaceUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsScreen(
    state: SpaceUiState,
    onCategorySelected: (SpaceCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onArticleClick: (SpaceArticle) -> Unit,
    onToggleBookmark: (SpaceArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        // Top Header & Radar Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(SpaceSurfaceElevated, SpaceSurface)
                    )
                )
                .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingRadarBeacon(sizeDp = 28)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "全球航天前沿实时监测",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "雷达扫描中 · ${state.lastRefreshedText}",
                                fontSize = 11.sp,
                                color = ElectricCyan
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = if (isSearchExpanded) ElectricCyan else TextSecondary
                            )
                        }

                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("refresh_button")
                        ) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = ElectricCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新",
                                    tint = ElectricCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metric Pill Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "监测动态",
                        value = "${state.articles.size} 条",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "关键词订阅",
                        value = "${state.keywords.count { it.isEnabled }} 个",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "预警推送",
                        value = "${state.alerts.size} 次",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search Bar (if expanded or query exists)
        AnimatedVisibility(visible = isSearchExpanded || state.searchQuery.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("news_search_input"),
                    placeholder = { Text("搜索航天动态、型号、探测器或机构...", fontSize = 13.sp, color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SpaceBorder,
                        focusedContainerColor = SpaceSurface,
                        unfocusedContainerColor = SpaceSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除", tint = TextSecondary)
                            }
                        }
                    }
                )
            }
        }

        // Category Horizontal Scroll Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpaceCategory.entries.forEach { category ->
                val isSelected = category == state.selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) ElectricCyan else SpaceSurfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan else SpaceBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("category_tab_${category.name}")
                ) {
                    Text(
                        text = category.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CosmicVoid else TextSecondary
                    )
                }
            }
        }

        // Articles Feed
        if (state.filteredArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "未搜索到相关前沿航天动态",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "尝试切换分类或刷新雷达获取最新突破",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredArticles, key = { it.id }) { article ->
                    val isBookmarked = state.bookmarkedIds.contains(article.id)
                    ArticleCard(
                        article = article,
                        isBookmarked = isBookmarked,
                        onClick = { onArticleClick(article) },
                        onToggleBookmark = { onToggleBookmark(article) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CosmicVoid.copy(alpha = 0.5f))
            .border(1.dp, SpaceBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 10.sp, color = TextMuted)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
        }
    }
}

@Composable
private fun ArticleCard(
    article: SpaceArticle,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlowingBorderCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("article_card_${article.id}"),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CosmicImpactBadge(text = article.impactBadge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = article.source,
                        fontSize = 11.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("bookmark_btn_${article.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "取消收藏" else "收藏",
                        tint = if (isBookmarked) PropulsionOrange else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = article.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = article.summary,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 19.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tags & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    article.tags.take(2).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SpaceSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                color = OrbitBlue
                            )
                        }
                    }
                }

                Text(
                    text = article.formattedDate,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
