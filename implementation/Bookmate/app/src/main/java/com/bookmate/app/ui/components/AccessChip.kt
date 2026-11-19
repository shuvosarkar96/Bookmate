package com.bookmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookmate.app.R
import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextPrimary

@Composable
fun AccessChip(accessType: AccessType, modifier: Modifier = Modifier) {
    val premium = accessType == AccessType.PREMIUM
    Box(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(50))
            .background(if (premium) Green else SecondaryContainerColor)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(if (premium) R.string.access_premium else R.string.access_free),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (premium) Cream else TextPrimary
        )
    }
}