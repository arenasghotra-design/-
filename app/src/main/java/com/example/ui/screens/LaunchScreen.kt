package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RocketLaunch
import com.example.ui.components.GlowingBorderCard
import com.example.ui.components.LaunchCountdownClock
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LaserRed
import com.example.ui.theme.OrbitBlue
import com.example.ui.theme.PropulsionOrange
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceElevated
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SpaceUiState

@Composable
fun LaunchScreen(
    state: SpaceUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        // Banner Header
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PropulsionOrange.copy(alpha = 0.2f))
                        .border(1.dp, PropulsionOrange.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = PropulsionOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "全球火箭发射全景看板",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "实时同步全球主要航天发射场点火倒计时",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Launches Timeline List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(state.launches, key = { it.id }) { launch ->
                LaunchCard(launch = launch)
            }
        }
    }
}

@Composable
private fun LaunchCard(
    launch: RocketLaunch,
    modifier: Modifier = Modifier
) {
    GlowingBorderCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("launch_card_${launch.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Rocket Name + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = launch.rocketName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(launch.status.colorHex).copy(alpha = 0.2f))
                        .border(1.dp, Color(launch.status.colorHex).copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = launch.status.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(launch.status.colorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mission Name
            Text(
                text = launch.missionName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-time Countdown Box
            LaunchCountdownClock(
                targetEpochMs = launch.scheduledEpochMs,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = launch.launchSite,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Orbit
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = OrbitBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "目标轨道: ${launch.orbitType}",
                    fontSize = 12.sp,
                    color = OrbitBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payload & Highlights Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SpaceSurfaceVariant)
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "载荷清单: ${launch.payloadInfo}",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "技术亮点: ${launch.technicalHighlights}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
