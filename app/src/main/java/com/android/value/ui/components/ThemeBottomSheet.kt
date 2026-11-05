package com.android.value.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.value.model.TimerTheme
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.SubtleGray

/**
 * Minimalist monochrome modal bottom sheet for switching themes while timer is running.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBottomSheet(
    selectedTheme: TimerTheme,
    onThemeSelected: (TimerTheme) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = PureBlack,
        scrimColor = Color(0x99000000),
        dragHandle = {
            Spacer(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(0.12f)
                    .height(4.dp)
                    .background(Color(0xFF333333), androidx.compose.foundation.shape.CircleShape)
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "CHOOSE THEME",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ThemeSelectorRow(
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected
            )
        }
    }
}

