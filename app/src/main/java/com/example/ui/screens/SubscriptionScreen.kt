package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.entity.AlertHistoryEntity
import com.example.data.local.entity.SubscriptionKeywordEntity
import com.example.data.model.SpaceArticle
import com.example.ui.components.GlowingBorderCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionScreen(
    state: SpaceUiState,
    onRequestNotificationPermission: () -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onToggleKeyword: (String, Boolean) -> Unit,
    onTestSimulatePush: (String) -> Unit,
    onScanAllMatches: () -> Unit,
    onSelectArticleById: (String) -> Unit,
    onClearAllAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputKeyword by remember { mutableStateOf("") }
    val popularRecommendations = listOf(
        "星舰", "嫦娥", "长征", "空间站", "可重复使用",
        "液氧甲烷", "天问", "深空探测", "SpaceX", "NASA"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Permission Status Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (state.hasNotificationPermission) SpaceSurfaceVariant
                        else PropulsionOrange.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (state.hasNotificationPermission) SpaceBorder
                        else PropulsionOrange.copy(alpha = 0.8f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.hasNotificationPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (state.hasNotificationPermission) ElectricCyan else PropulsionOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (state.hasNotificationPermission) "系统通知栏推送通道正常" else "系统通知权限尚未授予",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (state.hasNotificationPermission) "关键词命中时将立即向手机状态栏发送横幅通知" else "点击开启 Android 状态栏实时预警横幅推送",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (!state.hasNotificationPermission) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequestNotificationPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PropulsionOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("request_permission_button")
                        ) {
                            Text("开启权限", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Add Keyword Input Box
        item {
            GlowingBorderCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "添加自定义雷达订阅关键词",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "一旦有匹配的航天突破、新发射动态，雷达将第一时间推送预警",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputKeyword,
                            onValueChange = { inputKeyword = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("keyword_input_field"),
                            placeholder = { Text("例如：星舰、长征十号、月球基地...", fontSize = 13.sp, color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = SpaceBorder,
                                focusedContainerColor = SpaceSurfaceVariant,
                                unfocusedContainerColor = SpaceSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                if (inputKeyword.isNotBlank()) {
                                    onAddKeyword(inputKeyword)
                                    inputKeyword = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = CosmicVoid
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_keyword_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("订阅", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hot Recommendation Cloud
                    Text(
                        text = "热门航天关注热词（点击即时订阅）:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularRecommendations.forEach { tag ->
                            val alreadySubscribed = state.keywords.any { it.keyword == tag }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (alreadySubscribed) SpaceSurfaceElevated else SpaceSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (alreadySubscribed) ElectricCyan else SpaceBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (!alreadySubscribed) onAddKeyword(tag)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+ $tag",
                                        fontSize = 12.sp,
                                        color = if (alreadySubscribed) ElectricCyan else TextSecondary
                                    )
                                    if (alreadySubscribed) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✓", fontSize = 11.sp, color = ElectricCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Testing Buttons (Quick Test Push & Scan)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val firstKeyword = state.keywords.firstOrNull { it.isEnabled }?.keyword ?: "星舰"
                        onTestSimulatePush(firstKeyword)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_push_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PropulsionOrange.copy(alpha = 0.2f),
                        contentColor = PropulsionOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("测试模拟推送", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onScanAllMatches,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scan_matches_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan.copy(alpha = 0.2f),
                        contentColor = ElectricCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("全盘雷达扫描", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Subscribed Keywords List Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已订阅关键词 (${state.keywords.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "支持开关随时启闭",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        if (state.keywords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无订阅词，请添加或点选上方热门推荐", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(state.keywords, key = { it.keyword }) { kw ->
                KeywordItemCard(
                    keyword = kw,
                    onToggle = { onToggleKeyword(kw.keyword, it) },
                    onDelete = { onRemoveKeyword(kw.keyword) },
                    onTest = { onTestSimulatePush(kw.keyword) }
                )
            }
        }

        // Push Alert History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "雷达推送历史记录 (${state.alerts.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (state.alerts.isNotEmpty()) {
                    IconButton(
                        onClick = onClearAllAlerts,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "清空历史",
                            tint = TextMuted
                        )
                    }
                }
            }
        }

        if (state.alerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无推送触发记录，点击上方【测试模拟推送】可立即验证", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(state.alerts, key = { it.id }) { alert ->
                AlertHistoryCard(
                    alert = alert,
                    onClick = { onSelectArticleById(alert.articleId) }
                )
            }
        }
    }
}

@Composable
private fun KeywordItemCard(
    keyword: SubscriptionKeywordEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    GlowingBorderCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("keyword_card_${keyword.keyword}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (keyword.isEnabled) ElectricCyan.copy(alpha = 0.2f) else SpaceSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (keyword.isEnabled) ElectricCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = keyword.keyword,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (keyword.isEnabled) TextPrimary else TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "已命中库中 ${keyword.matchCount} 篇动态",
                        fontSize = 11.sp,
                        color = if (keyword.matchCount > 0) OrbitBlue else TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = keyword.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CosmicVoid,
                        checkedTrackColor = ElectricCyan,
                        uncheckedTrackColor = SpaceSurfaceVariant
                    ),
                    modifier = Modifier.testTag("toggle_switch_${keyword.keyword}")
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryCard(
    alert: AlertHistoryEntity,
    onClick: () -> Unit
) {
    val dateText = SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA).format(Date(alert.timestamp))

    GlowingBorderCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PropulsionOrange.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "命中: ${alert.matchedKeyword}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PropulsionOrange
                    )
                }

                Text(
                    text = dateText,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.articleTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2
            )
        }
    }
}
