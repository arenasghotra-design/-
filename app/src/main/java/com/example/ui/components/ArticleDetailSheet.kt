package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpaceArticle
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PropulsionOrange
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceBorderHighlight
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ArticleDetailSheet(
    article: SpaceArticle,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpaceSurface,
        scrimColor = CosmicVoid.copy(alpha = 0.75f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CosmicImpactBadge(text = article.impactBadge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = article.category.label,
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "已收藏" else "收藏",
                            tint = if (isBookmarked) PropulsionOrange else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, article.title)
                                putExtra(Intent.EXTRA_TEXT, "【星穹航天前沿】${article.title}\n\n${article.summary}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "分享航天要闻"))
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = article.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = article.source,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    text = article.formattedDate,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceSurfaceVariant)
                    .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "前沿突破核心摘要",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.summary,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Technical Specs Table if present
            if (article.technicalSpecs.isNotEmpty()) {
                Text(
                    text = "技术指标与关键参数",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicVoid.copy(alpha = 0.6f))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    article.technicalSpecs.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = key, fontSize = 13.sp, color = TextSecondary)
                            Text(
                                text = value,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Detailed Story
            Text(
                text = "深度技术背景与任务解析",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.details,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tag Cloud
            Text(
                text = "相关航天技术标签",
                fontSize = 13.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                article.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpaceSurfaceVariant)
                            .border(1.dp, SpaceBorderHighlight.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "# $tag",
                            fontSize = 12.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }
    }
}
