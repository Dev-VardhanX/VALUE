package com.devvardhan.value.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.core.AlarmSoundManager
import com.devvardhan.value.model.AlarmSound
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Sound selection and preview screen for timer alarms.
 * Strict monochrome aesthetic with three sound groups:
 * 1. DEFAULT (Phone default)
 * 2. VALUE TONES (Built-in audio tunes: Blast, Flow, Piece, Who, Wind)
 * 3. MY SOUNDS (Imported local audio files via SAF + Add from phone)
 */
@Composable
fun SoundScreen(
    selectedSoundId: String,
    importedSounds: List<AlarmSound.UserSound>,
    onSelectSound: (AlarmSound) -> Unit,
    onAddUserSound: (AlarmSound.UserSound) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val alarmSoundManager = remember { AlarmSoundManager(context) }
    val localFilesOnlyError = stringResource(R.string.error_local_files_only)
    val invalidAudioError = stringResource(R.string.error_invalid_audio)

    // Stop sound preview whenever leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            alarmSoundManager.stopPreview()
        }
    }

    // Check if alarm volume is currently muted
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val isAlarmVolumeOff = remember(audioManager) {
        try {
            audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) == 0
        } catch (e: Exception) {
            false
        }
    }

    // Document picker for importing local audio without copying files (offloaded to Dispatchers.IO)
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNullOrEmpty()) return@rememberLauncherForActivityResult

        val allowedAuthorities = setOf(
            "com.android.providers.media.documents",
            "com.android.providers.downloads.documents",
            "com.android.externalstorage.documents"
        )

        coroutineScope.launch(Dispatchers.IO) {
            for (uri in uris) {
                val authority = uri.authority
                if (authority !in allowedAuthorities) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, localFilesOnlyError, Toast.LENGTH_SHORT).show()
                    }
                    continue
                }

                val uriStr = uri.toString()
                if (importedSounds.any { it.uriString == uriStr }) {
                    continue // Do not duplicate
                }

                // Persist read access across app restarts
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed taking persistable permission for $uri", e)
                }

                var displayName = "Audio file"
                var sizeBytes = 0L
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIdx != -1) {
                                displayName = cursor.getString(nameIdx) ?: displayName
                            }
                            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeIdx != -1) {
                                sizeBytes = cursor.getLong(sizeIdx)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Metadata query error for $uri", e)
                }

                // Validate that file is playable audio and has positive duration
                var isValid = false
                val retriever = MediaMetadataRetriever()
                try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        retriever.setDataSource(pfd.fileDescriptor)
                        val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        val duration = durStr?.toLongOrNull() ?: 0L
                        if (duration > 0L) {
                            isValid = true
                        }
                    }
                } catch (e: Exception) {
                    isValid = false
                } finally {
                    try {
                        retriever.release()
                    } catch (e: Exception) {
                        // Ignore release failure
                    }
                }

                if (!isValid) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, invalidAudioError, Toast.LENGTH_SHORT).show()
                    }
                    continue
                }

                val userSound = AlarmSound.UserSound(
                    uriString = uriStr,
                    name = displayName,
                    sizeBytes = sizeBytes,
                    importTimestamp = System.currentTimeMillis()
                )
                withContext(Dispatchers.Main) {
                    onAddUserSound(userSound)
                    onSelectSound(userSound)
                    alarmSoundManager.previewSound(userSound)
                }
            }
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
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .semantics { contentDescription = "Back" }
                        .debugTestTag("btn_sound_back")
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
                    text = stringResource(R.string.sound_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 0.5.sp
                )
            }

            // Stream alarm volume warning if muted
            if (isAlarmVolumeOff) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.VolumeOff,
                        contentDescription = null,
                        tint = SubtleGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.sound_volume_off_hint),
                        fontSize = 12.sp,
                        color = SubtleGray
                    )
                }
            }

            // --- SECTION: DEFAULT ---
            SoundSectionHeader(text = stringResource(R.string.sound_section_default))

            SoundItemRow(
                title = stringResource(R.string.sound_phone_default),
                isSelected = selectedSoundId == AlarmSound.ID_PHONE_DEFAULT,
                onClick = {
                    onSelectSound(AlarmSound.PhoneDefault)
                    alarmSoundManager.previewSound(AlarmSound.PhoneDefault)
                },
                testTag = "row_sound_phone_default"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION: VALUE TONES ---
            SoundSectionHeader(text = stringResource(R.string.sound_section_value_tones))

            AlarmSound.BUILT_IN_TONES.forEachIndexed { index, tone ->
                SoundItemRow(
                    title = tone.title,
                    isSelected = selectedSoundId == tone.id,
                    onClick = {
                        onSelectSound(tone)
                        alarmSoundManager.previewSound(tone)
                    },
                    testTag = "row_sound_${tone.toneKey}"
                )
                if (index < AlarmSound.BUILT_IN_TONES.size - 1) {
                    SoundDivider()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION: MY SOUNDS ---
            SoundSectionHeader(text = stringResource(R.string.sound_section_my_sounds))

            importedSounds.forEach { userSound ->
                SoundItemRow(
                    title = userSound.name,
                    isSelected = selectedSoundId == userSound.id,
                    onClick = {
                        onSelectSound(userSound)
                        alarmSoundManager.previewSound(userSound)
                    },
                    testTag = "row_sound_user_${userSound.name}"
                )
                SoundDivider()
            }

            // "Add from phone" action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { pickAudioLauncher.launch(arrayOf("audio/*")) }
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .debugTestTag("row_add_sound"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = stringResource(R.string.sound_add_from_phone),
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.sound_add_from_phone),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = PureWhite,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SoundSectionHeader(text: String) {
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
private fun SoundDivider() {
    HorizontalDivider(
        color = BorderSubtle,
        thickness = 0.8.dp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SoundItemRow(
    title: String,
    isSelected: Boolean,
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
            text = title,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = PureWhite,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1f, fill = false)
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150))
        ) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = "Selected",
                tint = PureWhite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private const val TAG = "SoundScreen"

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Sound Screen Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewSoundScreenPortrait() {
    ValueTimerTheme {
        SoundScreen(
            selectedSoundId = "builtin_blast",
            importedSounds = listOf(
                AlarmSound.UserSound("uri://sample1", "Gentle Rain.mp3")
            ),
            onSelectSound = {},
            onAddUserSound = {},
            onBack = {}
        )
    }
}

@Preview(name = "Sound Screen Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewSoundScreenLandscape() {
    ValueTimerTheme {
        SoundScreen(
            selectedSoundId = AlarmSound.ID_PHONE_DEFAULT,
            importedSounds = emptyList(),
            onSelectSound = {},
            onAddUserSound = {},
            onBack = {}
        )
    }
}

@Preview(name = "Sound Screen Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewSoundScreenTablet() {
    ValueTimerTheme {
        SoundScreen(
            selectedSoundId = "builtin_who",
            importedSounds = listOf(
                AlarmSound.UserSound("uri://sample1", "Morning Breeze.wav"),
                AlarmSound.UserSound("uri://sample2", "Soft Bells.ogg")
            ),
            onSelectSound = {},
            onAddUserSound = {},
            onBack = {}
        )
    }
}

