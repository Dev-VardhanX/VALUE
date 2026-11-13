package com.devvardhan.value.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag

private val BodyTextColor = Color(0xFFD4D4D4)

/**
 * In-app Privacy Policy screen adhering to the strict monochrome aesthetic.
 * Subpage level: Main -> Settings -> Privacy policy.
 * Centered comfortable reading width (~600dp) for tablets and landscape orientations.
 */
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
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
                            .debugTestTag("btn_privacy_back")
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
                        text = stringResource(R.string.settings_privacy_policy),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = PureWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                // --- DOCUMENT TITLE ---
                Text(
                    text = stringResource(R.string.privacy_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite,
                    letterSpacing = 0.3.sp,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // --- EFFECTIVE DATE ---
                Text(
                    text = stringResource(R.string.privacy_effective_date),
                    fontSize = 13.sp,
                    color = SubtleGray,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- INTRO ---
                Text(
                    text = stringResource(R.string.privacy_intro),
                    fontSize = 14.sp,
                    color = BodyTextColor,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: SUMMARY ---
                PrivacySection(
                    heading = stringResource(R.string.privacy_section_summary),
                    body = stringResource(R.string.privacy_summary_body)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: INFORMATION WE DO NOT COLLECT ---
                PrivacySection(
                    heading = stringResource(R.string.privacy_section_info_not_collected),
                    body = stringResource(R.string.privacy_info_not_collected_body)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: DATA STORED ON YOUR DEVICE ---
                PrivacySection(
                    heading = stringResource(R.string.privacy_section_data_stored),
                    body = stringResource(R.string.privacy_data_stored_body)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: ALARM SOUNDS YOU ADD ---
                PrivacySection(
                    heading = stringResource(R.string.privacy_section_alarm_sounds),
                    body = stringResource(R.string.privacy_alarm_sounds_body)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: PERMISSIONS ---
                Text(
                    text = stringResource(R.string.privacy_section_permissions),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyBulletItem(text = stringResource(R.string.privacy_perm_notifications))
                PrivacyBulletItem(text = stringResource(R.string.privacy_perm_vibration))
                PrivacyBulletItem(text = stringResource(R.string.privacy_perm_foreground))
                PrivacyBulletItem(text = stringResource(R.string.privacy_perm_exact_alarms))
                PrivacyBulletItem(text = stringResource(R.string.privacy_perm_fullscreen_alerts))

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.privacy_permissions_closing),
                    fontSize = 14.sp,
                    color = BodyTextColor,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: CHILDREN ---
                PrivacySection(
                    heading = stringResource(R.string.privacy_section_children),
                    body = stringResource(R.string.privacy_children_body)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: CHANGES TO THIS POLICY ---
                PrivacySection(
                    heading = stringResource(R.string.privacy_section_changes),
                    body = stringResource(R.string.privacy_changes_body)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION: CONTACT ---
                PrivacySection(
                    heading = stringResource(R.string.privacy_section_contact),
                    body = stringResource(R.string.privacy_contact_body)
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun PrivacySection(
    heading: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = heading,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = PureWhite,
            letterSpacing = 0.3.sp,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = BodyTextColor,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun PrivacyBulletItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PureWhite,
            lineHeight = 22.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = BodyTextColor,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Privacy Policy Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewPrivacyPolicyPortrait() {
    ValueTimerTheme {
        PrivacyPolicyScreen(onBack = {})
    }
}

@Preview(name = "Privacy Policy Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewPrivacyPolicyLandscape() {
    ValueTimerTheme {
        PrivacyPolicyScreen(onBack = {})
    }
}

@Preview(name = "Privacy Policy Tablet Large Font", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1200, heightDp = 800, fontScale = 1.3f)
@Composable
private fun PreviewPrivacyPolicyTabletLargeFont() {
    ValueTimerTheme {
        PrivacyPolicyScreen(onBack = {})
    }
}

