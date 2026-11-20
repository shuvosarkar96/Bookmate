package com.bookmate.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.repository.SettingsRepository
import com.bookmate.app.ui.components.BackChevronIcon
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary
import com.bookmate.app.util.appVersionName
import com.bookmate.app.util.textSizeAt
import com.bookmate.app.util.textSizeIndex
import kotlin.math.roundToInt

private val DividerColor = Color(0xFFE3E3D9)

/** SCR-13 Settings. No bottom nav. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onAiInfo: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backLabel = stringResource(R.string.action_back)
    // The slider moves freely while dragging and is saved when released.
    var position by remember(state.textSize) { mutableFloatStateOf(textSizeIndex(state.textSize).toFloat()) }
    val previewSize = textSizeAt(position.roundToInt())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SideMargin, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .semantics { contentDescription = backLabel }
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                BackChevronIcon()
            }
            Text(
                text = stringResource(R.string.screen_settings),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SideMargin)
        ) {
            SectionLabel(stringResource(R.string.settings_section_account))
            SettingsCard {
                SettingRow(stringResource(R.string.settings_edit_profile), onEditProfile)
                RowDivider()
                SettingRow(stringResource(R.string.settings_change_password), onChangePassword)
            }

            SectionLabel(stringResource(R.string.settings_section_reading))
            SettingsCard {
                Text(
                    text = stringResource(R.string.settings_text_size),
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "A", fontSize = 13.sp, color = TextPrimary)
                    Slider(
                        value = position,
                        onValueChange = { position = it },
                        onValueChangeFinished = { viewModel.onTextSizePosition(position.roundToInt()) },
                        valueRange = 0f..SettingsRepository.TEXT_SIZES.lastIndex.toFloat(),
                        steps = SettingsRepository.TEXT_SIZES.size - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = Green,
                            activeTrackColor = Green,
                            activeTickColor = Cream
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(text = "A", fontSize = 20.sp, color = TextPrimary)
                }
                Text(
                    text = stringResource(R.string.settings_preview),
                    fontSize = previewSize.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                RowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_reminders),
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.remindersEnabled,
                        onCheckedChange = viewModel::onRemindersChange,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Green,
                            checkedThumbColor = Cream
                        )
                    )
                }
            }

            SectionLabel(stringResource(R.string.settings_section_about_ai))
            SettingsCard {
                SettingRow(stringResource(R.string.settings_ai_how), onAiInfo)
                RowDivider()
                Text(
                    text = stringResource(R.string.settings_ai_notice),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.5.dp, Green, RoundedCornerShape(26.dp))
                    .clickable {
                        viewModel.logout()
                        onLoggedOut()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.profile_logout),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_version, appVersionName(context)),
                fontSize = 12.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = DarkGreen,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        content = content
    )
}

@Composable
private fun SettingRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 16.sp, color = TextPrimary)
        Text(text = "›", fontSize = 20.sp, color = TextSecondary)
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerColor)
    )
}