package com.devvardhan.value.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.core.AppConfig
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag

/**
 * Settings screen adhering to the strict monochrome aesthetic.
 * Sections: TIMER, STOPWATCH, ABOUT.
 */
@Composable
fun SettingsScreen(
    timerStyleName: String,
    stopwatchStyleName: String,
    alarmSoundName: String,
    vibrationEnabled: Boolean,
    gradualVolumeEnabled: Boolean,
    onOpenTimerStyle: () -> Unit,
    onOpenStopwatchStyle: () -> Unit,
    onOpenSoundSettings: () -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onGradualVolumeToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val noBrowserError = stringResource(R.string.error_no_browser)

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .semantics { contentDescription = "Back" }
                        .debugTestTag("btn_settings_back")
                ) {
                    Icon(
                        imageVector = AppIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 0.5.sp
                )
            }

            // --- SECTION: TIMER ---
            SettingsSectionHeader(text = stringResource(R.string.settings_section_timer))

            SettingsNavRow(
                label = stringResource(R.string.settings_timer_style),
                value = timerStyleName,
                onClick = onOpenTimerStyle,
                testTag = "row_timer_style"
            )

            SettingsDivider()

            SettingsNavRow(
                label = stringResource(R.string.settings_alarm_sound),
                value = alarmSoundName,
                onClick = onOpenSoundSettings,
                testTag = "row_alarm_sound"
            )

            SettingsDivider()

            SettingsSwitchRow(
                label = stringResource(R.string.settings_vibration),
                checked = vibrationEnabled,
                onCheckedChange = onVibrationToggle,
                testTag = "switch_vibration"
            )

            SettingsDivider()

            SettingsSwitchRow(
                label = stringResource(R.string.settings_gradual_volume),
                subtitle = stringResource(R.string.settings_gradual_volume_desc),
                checked = gradualVolumeEnabled,
                onCheckedChange = onGradualVolumeToggle,
                testTag = "switch_gradual_volume"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION: STOPWATCH ---
            SettingsSectionHeader(text = stringResource(R.string.settings_section_stopwatch))

            SettingsNavRow(
                label = stringResource(R.string.settings_stopwatch_style),
                value = stopwatchStyleName,
                onClick = onOpenStopwatchStyle,
                testTag = "row_stopwatch_style"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION: ABOUT ---
            SettingsSectionHeader(text = stringResource(R.string.settings_section_about))

            SettingsNavRow(
                label = stringResource(R.string.settings_privacy_policy),
                value = "",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, AppConfig.PRIVACY_POLICY_URL.toUri())
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, noBrowserError, Toast.LENGTH_SHORT).show()
                    }
                },
                testTag = "row_privacy_policy"
            )

            SettingsDivider()

            SettingsInfoRow(
                label = stringResource(R.string.settings_version),
                value = versionName
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = SubtleGray,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = BorderSubtle,
        thickness = 0.8.dp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsNavRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .debugTestTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = PureWhite,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1f, fill = false)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = SubtleGray,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = SubtleGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 54.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .debugTestTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = PureWhite,
                maxLines = 1,
                softWrap = false
            )
            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SubtleGray,
                    letterSpacing = 0.2.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureBlack,
                checkedTrackColor = PureWhite,
                checkedBorderColor = PureWhite,
                uncheckedThumbColor = SubtleGray,
                uncheckedTrackColor = Color(0xFF141414),
                uncheckedBorderColor = BorderSubtle
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = PureWhite,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = SubtleGray,
            maxLines = 1,
            softWrap = false
        )
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Settings Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewSettingsPortrait() {
    ValueTimerTheme {
        SettingsScreen(
            timerStyleName = "Hourglass",
            stopwatchStyleName = "Concentric Rings",
            alarmSoundName = "Blast",
            vibrationEnabled = true,
            gradualVolumeEnabled = false,
            onOpenTimerStyle = {},
            onOpenStopwatchStyle = {},
            onOpenSoundSettings = {},
            onVibrationToggle = {},
            onGradualVolumeToggle = {},
            onBack = {}
        )
    }
}

@Preview(name = "Settings Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewSettingsLandscape() {
    ValueTimerTheme {
        SettingsScreen(
            timerStyleName = "Hourglass",
            stopwatchStyleName = "Concentric Rings",
            alarmSoundName = "Blast",
            vibrationEnabled = true,
            gradualVolumeEnabled = true,
            onOpenTimerStyle = {},
            onOpenStopwatchStyle = {},
            onOpenSoundSettings = {},
            onVibrationToggle = {},
            onGradualVolumeToggle = {},
            onBack = {}
        )
    }
}

@Preview(name = "Settings Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewSettingsTablet() {
    ValueTimerTheme {
        SettingsScreen(
            timerStyleName = "Flip Clock",
            stopwatchStyleName = "Three Box",
            alarmSoundName = "Phone default",
            vibrationEnabled = true,
            gradualVolumeEnabled = false,
            onOpenTimerStyle = {},
            onOpenStopwatchStyle = {},
            onOpenSoundSettings = {},
            onVibrationToggle = {},
            onGradualVolumeToggle = {},
            onBack = {}
        )
    }
}

