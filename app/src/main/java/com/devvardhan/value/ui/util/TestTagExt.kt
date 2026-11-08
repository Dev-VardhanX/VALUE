package com.devvardhan.value.ui.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.devvardhan.value.BuildConfig

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.rootTestTagsAsResourceId(): Modifier {
    return if (BuildConfig.DEBUG) {
        this.semantics {
            testTagsAsResourceId = true
        }
    } else {
        this
    }
}

fun Modifier.debugTestTag(tag: String): Modifier {
    return if (BuildConfig.DEBUG) {
        this.testTag(tag)
    } else {
        this
    }
}

