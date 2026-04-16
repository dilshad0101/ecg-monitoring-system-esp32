package com.app.healthmonitor.home

import android.graphics.drawable.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.healthmonitor.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel = viewModel(),
    modifier: Modifier
){

    LaunchedEffect(Unit) {
        viewModel.startAutoRefresh()
    }
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLiveSync by viewModel.isLiveSync.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("HEALTH MONITOR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(//horizontal = 30.dp,
                                vertical = 0.dp),
                    )},
                navigationIcon = {
                    FilledIconButton(
                        onClick = {},
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(244,244,248)
                        )
                        ) {
                        Icon(painter = painterResource(R.drawable.baseline_menu_24),"menu")
                    }
                },
                actions = {
                    FilledIconButton(
                        onClick = {},
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(244,244,248)
                        )
                    ) {
                        Icon(painter = painterResource(R.drawable.round_person_24),"menu")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent ),
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp)

                //expandedHeight = 30.dp
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)

    ) {
        LazyColumn (
            modifier = modifier.padding(it)
        ) {
            this.item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .shadow(1.dp,
                            shape = RoundedCornerShape(10),
                            ambientColor = Color.LightGray,
                            spotColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color(255,251,254)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)
                    ) {
                        Text(
                            text = if(isLiveSync)"REAL-TIME PULSE" else "LAST RECORDED PULSE",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(start = 15.dp)
                        ) {
                            Text(
                                    text = if (uiState.bpm.toString() != "0") uiState.bpm.toString() else "N/A",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " BPM",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LiveEcgIndicator(isLiveSync)

                    }
                }
                Spacer(Modifier.padding(vertical = 15.dp))
                Card(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .shadow(1.dp,
                            shape = RoundedCornerShape(10),
                            ambientColor = Color.LightGray,
                            spotColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color(255,251,254)),
                    elevation = CardDefaults.cardElevation(0.dp),

                    ) {
                    ProgressCard()

                }
                Spacer(Modifier.padding(vertical = 15.dp))

                HorizontalCard(
                    icon = painterResource(R.drawable.outline_airwave_24),
                    title = "HRV",
                    value = "50ms",
                )

                HorizontalCard(
                    icon = painterResource(R.drawable.outline_mood_24),
                    title = "STRESS INDICATOR",
                    value = "LOW",
                    )
                HorizontalCard(
                    icon = painterResource(R.drawable.baseline_nights_stay_24),
                    title = "REST QUALITY",
                    value = "8.2Hrs",
                )
                Spacer(Modifier.padding(vertical = 15.dp))
                ECGWaveform()
                Spacer(Modifier.padding(vertical = 15.dp))
                InsightSection()

            }

        }


//        Text(
//            text = uiState.firstDieValue.toString(),
//            modifier = modifier.clickable(
//                true,
//                onClick = {
//                    viewModel.rollDice()
//                })
//        )

    }
}

@Composable
fun ProgressCard(progress: Float = 0.85f) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column() {
            Text(
                text = "RHYTHMIC REGULARITY",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(contentAlignment = Alignment.Center) {


                Canvas(modifier = Modifier.size(150.dp)) {

                    val strokeWidth = 12.dp.toPx()
                    val radius = size.minDimension / 2

                    // Background circle
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress arc
                    drawArc(
                        color = Color(0xFF1E88E5),
                        startAngle = -90f,
                        sweepAngle = 360 * progress,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()} %",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "WELLNESS",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your recovery is Optimal today. Increase activity by 15%.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}