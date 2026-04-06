package com.app.healthmonitor.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.healthmonitor.R
import kotlin.io.path.Path
import kotlin.io.path.moveTo
import kotlin.math.PI
import kotlin.math.sin

// EcgWaveformScreen.kt
@Composable
fun ECGWaveform() {
    val bgColor = Color(0xFFE8F4FA)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
               // .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Text(
                text = "Latest ECG Waveform",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B2A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Captured today at 08:42 AM • Source: Wearable Device",
                fontSize = 13.sp,
                color = Color(0xFF5A7A8A),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Export button
            Button(
                onClick = { /* handle export */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4FA0)
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_download_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Full Report",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ECG waveform card
            EcgCard()

            Spacer(modifier = Modifier.height(20.dp))

            // Status badge
//            NormalSinusBadge()
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Metric chips
//            EcgChip(label = "P-WAVE: 0.12S")
//            Spacer(modifier = Modifier.height(10.dp))
//            EcgChip(label = "QRS COMPLEX: 0.08S")
        }
    }
}
// EcgCard.kt
@Composable
fun EcgCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        EcgWaveformCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                    .padding(12.dp)
            )
        }
    }

    @Composable
    fun EcgWaveformCanvas(modifier: Modifier = Modifier) {

        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            val mid = h * 0.52f
            val amp = h * 0.38f

            // Grid
            val gridColor = Color(0xFFDDE8F0)
            val cols = 10; val rows = 6
            for (i in 0..cols) {
                drawLine(gridColor, Offset(i * w / cols, 0f), Offset(i * w / cols, h), 1f)
            }
            for (i in 0..rows) {
                drawLine(gridColor, Offset(0f, i * h / rows), Offset(w, i * h / rows), 1f)
            }

            // Dashed vertical markers at R peaks
            val numBeats = 3
            val beatPeriod = 0.22f
            val totalT = numBeats * beatPeriod
            val peakPhase = 0.222f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))

            for (b in 0 until numBeats) {
                val peakT = b * beatPeriod + peakPhase
                val x = (peakT / totalT) * w
                drawLine(
                    color = Color(0xFFB0C8E0),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.5f,
                    pathEffect = dashEffect
                )
            }

            // ECG waveform path
            val path = Path()
            val steps = (w * 2).toInt()
            for (i in 0..steps) {
                val x = (i.toFloat() / steps) * w
                val t = (i.toFloat() / steps) * totalT
                val p = (t / beatPeriod) % 1f
                val y = when {
                    p < 0.08f  -> mid - amp * 0.12f * sin(p / 0.08f * PI.toFloat())
                    p < 0.16f  -> mid + amp * 0.04f * sin((p - 0.08f) / 0.08f * PI.toFloat())
                    p < 0.20f  -> mid
                    p < 0.215f -> mid + amp * 0.22f * sin((p - 0.20f) / 0.015f * PI.toFloat())
                    p < 0.230f -> mid - amp * 1.00f * sin((p - 0.215f) / 0.015f * PI.toFloat())
                    p < 0.245f -> mid + amp * 0.28f * sin((p - 0.230f) / 0.015f * PI.toFloat())
                    p < 0.260f -> mid - amp * 0.08f * sin((p - 0.245f) / 0.015f * PI.toFloat())
                    p < 0.300f -> mid
                    p < 0.440f -> mid - amp * 0.18f * sin((p - 0.30f) / 0.14f * PI.toFloat())
                    else       -> mid + amp * 0.015f * sin(p * 40f)
                }
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = Color(0xFF1A4FA0),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }