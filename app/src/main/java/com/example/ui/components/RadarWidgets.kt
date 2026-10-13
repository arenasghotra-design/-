package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LaserRed
import com.example.ui.theme.OrbitBlue
import com.example.ui.theme.PropulsionOrange
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PulsingRadarBeacon(
    modifier: Modifier = Modifier,
    sizeDp: Int = 36
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxR = size.width / 2

            // Outer pulse wave
            drawCircle(
                color = ElectricCyan.copy(alpha = pulseAlpha),
                radius = maxR * pulseRadius,
                center = center,
                style = Stroke(width = 2f)
            )

            // Inner orbit circles
            drawCircle(
                color = OrbitBlue.copy(alpha = 0.35f),
                radius = maxR * 0.7f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Radar sweep line
            val rad = Math.toRadians(sweepAngle.toDouble())
            val endX = center.x + (maxR * 0.9f) * cos(rad).toFloat()
            val endY = center.y + (maxR * 0.9f) * sin(rad).toFloat()
            drawLine(
                color = ElectricCyan.copy(alpha = 0.85f),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 2.5f
            )

            // Core center dot
            drawCircle(
                color = ElectricCyan,
                radius = 3.5f,
                center = center
            )
        }
    }
}

@Composable
fun LaunchCountdownClock(
    targetEpochMs: Long,
    modifier: Modifier = Modifier
) {
    var remainingMs by remember(targetEpochMs) {
        mutableLongStateOf(maxOf(0L, targetEpochMs - System.currentTimeMillis()))
    }

    LaunchedEffect(targetEpochMs) {
        while (true) {
            remainingMs = maxOf(0L, targetEpochMs - System.currentTimeMillis())
            delay(1000)
        }
    }

    val totalSecs = remainingMs / 1000
    val days = totalSecs / (24 * 3600)
    val hours = (totalSecs % (24 * 3600)) / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicVoid.copy(alpha = 0.85f))
            .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CountdownUnit(value = String.format("%02d", days), label = "天")
        Text(":", color = ElectricCyan, fontWeight = FontWeight.Bold)
        CountdownUnit(value = String.format("%02d", hours), label = "时")
        Text(":", color = ElectricCyan, fontWeight = FontWeight.Bold)
        CountdownUnit(value = String.format("%02d", minutes), label = "分")
        Text(":", color = ElectricCyan, fontWeight = FontWeight.Bold)
        CountdownUnit(value = String.format("%02d", seconds), label = "秒", isHighlighted = true)
    }
}

@Composable
private fun CountdownUnit(
    value: String,
    label: String,
    isHighlighted: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = if (isHighlighted) PropulsionOrange else ElectricCyan
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
fun CosmicImpactBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        PropulsionOrange.copy(alpha = 0.25f),
                        LaserRed.copy(alpha = 0.25f)
                    )
                )
            )
            .border(1.dp, PropulsionOrange.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PropulsionOrange
        )
    }
}

@Composable
fun GlowingBorderCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SpaceBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SpaceSurface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    } else {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SpaceSurface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    }

    Box(modifier = cardModifier) {
        content()
    }
}
