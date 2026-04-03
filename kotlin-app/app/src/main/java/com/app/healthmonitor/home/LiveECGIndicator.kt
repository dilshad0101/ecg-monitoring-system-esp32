package com.app.healthmonitor.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.io.path.Path

@Composable
fun LiveEcgIndicator(
    isLive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isLive) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Transparent, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        Canvas(modifier = Modifier.matchParentSize()) {

            val path = Path()
            val width = size.width
            val height = size.height
            val midY = height / 2

            val cycleWidth = width / 3   // one ECG cycle
            val offsetX = if (isLive) shift * width else 0f

            path.moveTo(-offsetX, midY)

            var x = -offsetX

            while (x < width + cycleWidth) {

                // --- ECG Pattern ---
                path.lineTo(x + 10, midY)               // baseline
                path.lineTo(x + 20, midY - 10)          // P wave up
                path.lineTo(x + 30, midY)               // back

                path.lineTo(x + 40, midY)               // baseline

                path.lineTo(x + 50, midY + 20)          // Q dip
                path.lineTo(x + 60, midY - 40)          // R peak (sharp spike)
                path.lineTo(x + 70, midY + 25)          // S dip
                path.lineTo(x + 80, midY)               // back to baseline

                path.lineTo(x + 95, midY + 10)          // T wave
                path.lineTo(x + 110, midY)              // baseline

                x += cycleWidth
            }

            drawPath(
                path = path,
                color = Color(0xFF0F766E),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }

        // Status Pill
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(
                    color = if (isLive) Color(0xFF9EDBD6) else Color.Gray,
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (isLive) Color(0xFF0F766E) else Color.DarkGray,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isLive) "LIVE SYNCING" else "OFFLINE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F766E)
            )
        }
    }
}