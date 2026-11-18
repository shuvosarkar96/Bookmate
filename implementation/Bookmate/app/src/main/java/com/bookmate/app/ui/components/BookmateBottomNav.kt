package com.bookmate.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bookmate.app.R
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.NavActive
import com.bookmate.app.ui.theme.NavContainer
import com.bookmate.app.ui.theme.TextPrimary

enum class BottomNavTab(@StringRes val labelRes: Int) {
    Home(R.string.nav_home),
    Search(R.string.nav_search),
    Ai(R.string.nav_ai),
    MyBooks(R.string.nav_my_books),
    Profile(R.string.nav_profile)
}

@Composable
fun BookmateBottomNav(
    selected: BottomNavTab,
    onSelect: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.NavHeight),
        shape = RoundedCornerShape(36.dp),
        color = NavContainer
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.entries.forEach { tab ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    NavItem(
                        tab = tab,
                        selected = tab == selected,
                        onClick = { onSelect(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(tab: BottomNavTab, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) NavActive else Color.Transparent
    Column(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NavIcon(tab)
        Text(
            text = stringResource(tab.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = TextPrimary,
            maxLines = 1
        )
    }
}

private val iconStroke = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val homePath = PathParser().parsePathString("M2 16 L14 5 L26 16 M5 14 V28 H23 V14").toPath()
private val searchHandle = PathParser().parsePathString("M17 16 L23 22").toPath()
private val sparklePath = PathParser()
    .parsePathString("M14 0 L17.5 10.5 L28 14 L17.5 17.5 L14 28 L10.5 17.5 L0 14 L10.5 10.5 Z")
    .toPath()
private val profileBody = PathParser().parsePathString("M2 28 C2 19 26 19 26 28").toPath()

@Composable
private fun NavIcon(tab: BottomNavTab) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val factor = size.width / 28f
        scale(factor, factor, pivot = Offset.Zero) {
            when (tab) {
                BottomNavTab.Home -> drawPath(homePath, TextPrimary, style = iconStroke)
                BottomNavTab.Search -> {
                    drawCircle(TextPrimary, radius = 7f, center = Offset(12f, 11f), style = iconStroke)
                    drawPath(searchHandle, TextPrimary, style = iconStroke)
                }
                BottomNavTab.Ai -> drawPath(sparklePath, TextPrimary, style = iconStroke)
                BottomNavTab.MyBooks -> listOf(2f, 11f, 20f).forEach { x ->
                    drawRoundRect(
                        color = TextPrimary,
                        topLeft = Offset(x, 4f),
                        size = Size(6f, 24f),
                        cornerRadius = CornerRadius(1f, 1f),
                        style = iconStroke
                    )
                }
                BottomNavTab.Profile -> {
                    drawCircle(TextPrimary, radius = 6f, center = Offset(14f, 10f), style = iconStroke)
                    drawPath(profileBody, TextPrimary, style = iconStroke)
                }
            }
        }
    }
}