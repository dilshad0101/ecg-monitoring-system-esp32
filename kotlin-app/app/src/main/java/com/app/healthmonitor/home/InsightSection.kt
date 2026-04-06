package com.app.healthmonitor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InsightSection(){
    Column(
        modifier = Modifier.padding(horizontal =32.dp)
    ) {
        Text(text = "Deep Analysis Report",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp))
        Text(text = "Deep Analysis interprets your ECG data to provide meaningful insights about your heart rhythm, variability, and recovery state. It helps you understand your current physiological condition beyond raw numbers.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify)
        Spacer(Modifier.height(10.dp))
        InsightCard(
            title = "Heart Rate Variability (HRV)" ,
            metric = Pair("54","ms"),
            description = "Your heart rate variability is high, which indicates a well-balanced and responsive autonomic nervous system. This typically reflects a relaxed physiological state, good recovery, and low stress levels. Your body is adapting efficiently to internal and external conditions, suggesting you are well-rested and in a healthy state.",
        )
        InsightCard(
            title = "Rhythmic Regularity",
            metric = Pair("88","%"),
            description = "Your heart rhythm appears consistent, with minimal variation between consecutive beats. This indicates stable cardiac activity. Minor variations are normal and reflect healthy autonomic regulation."
        )
//        InsightCard(
//            title = "Recovery & Stress Analysis",
//            metric = Pair("88","%"),
//            description = "Your heart rhythm appears consistent, with minimal variation between consecutive beats. This indicates stable cardiac activity. Minor variations are normal and reflect healthy autonomic regulation."
//        )
        // Graph of Short Term trend of HRV and Heart Rate BPM


    }
}

@Composable
fun InsightCard(
    title: String,
    metric: Pair<String,String>,
    description: String,
){
    Card(
        modifier = Modifier
            //.padding(horizontal = 32.dp)
            .padding(bottom = 10.dp)

            .shadow(1.dp,
                shape = RoundedCornerShape(10),
                ambientColor = Color.LightGray,
                spotColor = Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color(255,251,254)),
        elevation = CardDefaults.cardElevation(0.dp),

        ){
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(start = 15.dp)
            ) {
                Text(
                    text = metric.first,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " "+metric.second,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(text = description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify,

                )
        }

    }
}