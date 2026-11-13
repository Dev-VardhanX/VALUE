package com.devvardhan.value.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lightweight, hand-crafted vector icons replacing the heavy material-icons-extended dependency.
 */
object AppIcons {
    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(19f, 13f)
                horizontalLineTo(13f)
                verticalLineTo(19f)
                horizontalLineTo(11f)
                verticalLineTo(13f)
                horizontalLineTo(5f)
                verticalLineTo(11f)
                horizontalLineTo(11f)
                verticalLineTo(5f)
                horizontalLineTo(13f)
                verticalLineTo(11f)
                horizontalLineTo(19f)
                verticalLineTo(13f)
                close()
            }
        }.build()
    }

    val Remove: ImageVector by lazy {
        ImageVector.Builder(
            name = "Remove",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(19f, 13f)
                horizontalLineTo(5f)
                verticalLineTo(11f)
                horizontalLineTo(19f)
                verticalLineTo(13f)
                close()
            }
        }.build()
    }

    val Fullscreen: ImageVector by lazy {
        ImageVector.Builder(
            name = "Fullscreen",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(7f, 14f)
                horizontalLineTo(5f)
                verticalLineTo(19f)
                horizontalLineTo(10f)
                verticalLineTo(17f)
                horizontalLineTo(7f)
                verticalLineTo(14f)
                close()
                moveTo(5f, 10f)
                horizontalLineTo(7f)
                verticalLineTo(7f)
                horizontalLineTo(10f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(10f)
                close()
                moveTo(19f, 14f)
                horizontalLineTo(17f)
                verticalLineTo(17f)
                horizontalLineTo(14f)
                verticalLineTo(19f)
                horizontalLineTo(19f)
                verticalLineTo(14f)
                close()
                moveTo(14f, 5f)
                verticalLineTo(7f)
                horizontalLineTo(17f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                verticalLineTo(5f)
                horizontalLineTo(14f)
                close()
            }
        }.build()
    }

    val FullscreenExit: ImageVector by lazy {
        ImageVector.Builder(
            name = "FullscreenExit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(5f, 16f)
                horizontalLineTo(8f)
                verticalLineTo(19f)
                horizontalLineTo(10f)
                verticalLineTo(14f)
                horizontalLineTo(5f)
                verticalLineTo(16f)
                close()
                moveTo(8f, 8f)
                horizontalLineTo(5f)
                verticalLineTo(10f)
                horizontalLineTo(10f)
                verticalLineTo(5f)
                horizontalLineTo(8f)
                verticalLineTo(8f)
                close()
                moveTo(14f, 19f)
                horizontalLineTo(16f)
                verticalLineTo(16f)
                horizontalLineTo(19f)
                verticalLineTo(14f)
                horizontalLineTo(14f)
                verticalLineTo(19f)
                close()
                moveTo(16f, 8f)
                verticalLineTo(5f)
                horizontalLineTo(14f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                verticalLineTo(8f)
                horizontalLineTo(16f)
                close()
            }
        }.build()
    }

    val Palette: ImageVector by lazy {
        ImageVector.Builder(
            name = "Palette",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                curveTo(3f, 14.12f, 3.74f, 16.07f, 4.97f, 17.61f)
                lineTo(4.35f, 18.2f)
                curveTo(3.96f, 18.59f, 3.96f, 19.22f, 4.35f, 19.61f)
                curveTo(4.74f, 20f, 5.37f, 20f, 5.76f, 19.61f)
                lineTo(7.66f, 17.71f)
                curveTo(8.98f, 18.52f, 10.45f, 19f, 12f, 19f)
                curveTo(16.97f, 19f, 21f, 14.97f, 21f, 10f)
                curveTo(21f, 6.13f, 17f, 3f, 12f, 3f)
                close()
                moveTo(6.5f, 12f)
                curveTo(5.67f, 12f, 5f, 11.33f, 5f, 10.5f)
                reflectiveCurveTo(5.67f, 9f, 6.5f, 9f)
                reflectiveCurveTo(8f, 9.67f, 8f, 10.5f)
                reflectiveCurveTo(7.33f, 12f, 6.5f, 12f)
                close()
                moveTo(9.5f, 8f)
                curveTo(8.67f, 8f, 8f, 7.33f, 8f, 6.5f)
                reflectiveCurveTo(8.67f, 5f, 9.5f, 5f)
                reflectiveCurveTo(11f, 5.67f, 11f, 6.5f)
                reflectiveCurveTo(10.33f, 8f, 9.5f, 8f)
                close()
                moveTo(14.5f, 8f)
                curveTo(13.67f, 8f, 13f, 7.33f, 13f, 6.5f)
                reflectiveCurveTo(13.67f, 5f, 14.5f, 5f)
                reflectiveCurveTo(16f, 5.67f, 16f, 6.5f)
                reflectiveCurveTo(15.33f, 8f, 14.5f, 8f)
                close()
                moveTo(17.5f, 12f)
                curveTo(16.67f, 12f, 16f, 11.33f, 16f, 10.5f)
                reflectiveCurveTo(16.67f, 9f, 17.5f, 9f)
                reflectiveCurveTo(19f, 9.67f, 19f, 10.5f)
                reflectiveCurveTo(18.33f, 12f, 17.5f, 12f)
                close()
            }
        }.build()
    }

    val Stopwatch: ImageVector by lazy {
        ImageVector.Builder(
            name = "Stopwatch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Top crown
                moveTo(9f, 1f)
                horizontalLineTo(15f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                close()
                // Main body
                moveTo(13f, 3f)
                horizontalLineTo(11f)
                verticalLineTo(5.05f)
                curveTo(6.94f, 5.51f, 3.5f, 9.35f, 3.5f, 14f)
                curveTo(3.5f, 18.69f, 7.31f, 22.5f, 12f, 22.5f)
                reflectiveCurveTo(20.5f, 18.69f, 20.5f, 14f)
                curveTo(20.5f, 9.35f, 17.06f, 5.51f, 13f, 5.05f)
                verticalLineTo(3f)
                close()
                // Inner dial cutout
                moveTo(12f, 20.5f)
                curveTo(8.41f, 20.5f, 5.5f, 17.59f, 5.5f, 14f)
                reflectiveCurveTo(8.41f, 7.5f, 12f, 7.5f)
                reflectiveCurveTo(18.5f, 10.41f, 18.5f, 14f)
                reflectiveCurveTo(15.59f, 20.5f, 12f, 20.5f)
                close()
                // Hand indicator
                moveTo(13f, 10f)
                horizontalLineTo(11f)
                verticalLineTo(15f)
                lineTo(15.25f, 17.52f)
                lineTo(16f, 16.29f)
                lineTo(13f, 14.5f)
                verticalLineTo(10f)
                close()
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(19f, 6.41f)
                lineTo(17.59f, 5f)
                lineTo(12f, 10.59f)
                lineTo(6.41f, 5f)
                lineTo(5f, 6.41f)
                lineTo(10.59f, 12f)
                lineTo(5f, 17.59f)
                lineTo(6.41f, 19f)
                lineTo(12f, 13.41f)
                lineTo(17.59f, 19f)
                lineTo(19f, 17.59f)
                lineTo(13.41f, 12f)
                close()
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20f, 11f)
                horizontalLineTo(7.83f)
                lineTo(13.42f, 5.41f)
                lineTo(12f, 4f)
                lineTo(4f, 12f)
                lineTo(12f, 20f)
                lineTo(13.41f, 18.59f)
                lineTo(7.83f, 13f)
                horizontalLineTo(20f)
                verticalLineTo(11f)
                close()
            }
        }.build()
    }

    val Flag: ImageVector by lazy {
        ImageVector.Builder(
            name = "Flag",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(14.4f, 6f)
                lineTo(14f, 4f)
                horizontalLineTo(5f)
                verticalLineTo(21f)
                horizontalLineTo(7f)
                verticalLineTo(14f)
                horizontalLineTo(12.6f)
                lineTo(13f, 16f)
                horizontalLineTo(20f)
                verticalLineTo(6f)
                horizontalLineTo(14.4f)
                close()
            }
        }.build()
    }

    val Reset: ImageVector by lazy {
        ImageVector.Builder(
            name = "Reset",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 5f)
                verticalLineTo(1f)
                lineTo(7f, 6f)
                lineTo(12f, 11f)
                verticalLineTo(7f)
                curveTo(15.31f, 7f, 18f, 9.69f, 18f, 13f)
                curveTo(18f, 16.31f, 15.31f, 19f, 12f, 19f)
                curveTo(8.69f, 19f, 6f, 16.31f, 6f, 13f)
                horizontalLineTo(4f)
                curveTo(4f, 17.42f, 7.58f, 21f, 12f, 21f)
                curveTo(16.42f, 21f, 20f, 17.42f, 20f, 13f)
                curveTo(20f, 8.58f, 16.42f, 5f, 12f, 5f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.63f, 19.2f, 12.32f, 19.2f, 12f)
                reflectiveCurveTo(19.18f, 11.37f, 19.14f, 11.06f)
                lineTo(21.28f, 9.39f)
                curveTo(21.47f, 9.24f, 21.53f, 8.97f, 21.41f, 8.76f)
                lineTo(19.39f, 5.26f)
                curveTo(19.27f, 5.05f, 19.01f, 4.96f, 18.79f, 5.05f)
                lineTo(16.27f, 6.06f)
                curveTo(15.75f, 5.66f, 15.18f, 5.34f, 14.56f, 5.08f)
                lineTo(14.18f, 2.4f)
                curveTo(14.14f, 2.16f, 13.93f, 2f, 13.69f, 2f)
                horizontalLineTo(9.65f)
                curveTo(9.41f, 2f, 9.2f, 2.16f, 9.17f, 2.4f)
                lineTo(8.78f, 5.08f)
                curveTo(8.16f, 5.34f, 7.6f, 5.66f, 7.07f, 6.06f)
                lineTo(4.55f, 5.05f)
                curveTo(4.33f, 4.96f, 4.07f, 5.05f, 3.95f, 5.26f)
                lineTo(1.93f, 8.76f)
                curveTo(1.81f, 8.97f, 1.87f, 9.24f, 2.06f, 9.39f)
                lineTo(4.2f, 11.06f)
                curveTo(4.16f, 11.37f, 4.14f, 11.68f, 4.14f, 12f)
                reflectiveCurveTo(4.16f, 12.63f, 4.2f, 12.94f)
                lineTo(2.06f, 14.61f)
                curveTo(1.87f, 14.76f, 1.81f, 15.03f, 1.93f, 15.24f)
                lineTo(3.95f, 18.74f)
                curveTo(4.07f, 18.95f, 4.33f, 19.04f, 4.55f, 18.95f)
                lineTo(7.07f, 17.94f)
                curveTo(7.6f, 18.34f, 8.16f, 18.66f, 8.78f, 18.92f)
                lineTo(9.17f, 21.6f)
                curveTo(9.2f, 21.84f, 9.41f, 22f, 9.65f, 22f)
                horizontalLineTo(13.69f)
                curveTo(13.93f, 22f, 14.14f, 21.84f, 14.18f, 21.6f)
                lineTo(14.56f, 18.92f)
                curveTo(15.18f, 18.66f, 15.75f, 18.34f, 16.27f, 17.94f)
                lineTo(18.79f, 18.95f)
                curveTo(19.01f, 19.04f, 19.27f, 18.95f, 19.39f, 18.74f)
                lineTo(21.41f, 15.24f)
                curveTo(21.53f, 15.03f, 21.47f, 14.76f, 21.28f, 14.61f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(12f, 15.5f)
                curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
                reflectiveCurveTo(10.07f, 8.5f, 12f, 8.5f)
                reflectiveCurveTo(15.5f, 10.07f, 15.5f, 12f)
                reflectiveCurveTo(13.93f, 15.5f, 12f, 15.5f)
                close()
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9.29f, 6.71f)
                lineTo(13.88f, 11.29f)
                curveTo(14.27f, 11.68f, 14.27f, 12.31f, 13.88f, 12.7f)
                lineTo(9.29f, 17.29f)
                curveTo(8.9f, 17.68f, 8.27f, 17.68f, 7.88f, 17.29f)
                curveTo(7.49f, 16.9f, 7.49f, 16.27f, 7.88f, 15.88f)
                lineTo(11.76f, 12f)
                lineTo(7.88f, 8.12f)
                curveTo(7.49f, 7.73f, 7.49f, 7.1f, 7.88f, 6.71f)
                curveTo(8.27f, 6.32f, 8.9f, 6.32f, 9.29f, 6.71f)
                close()
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9f, 16.17f)
                lineTo(4.83f, 12f)
                lineTo(3.41f, 13.41f)
                lineTo(9f, 19f)
                lineTo(21f, 7f)
                lineTo(19.59f, 5.59f)
                close()
            }
        }.build()
    }

    val Stop: ImageVector by lazy {
        ImageVector.Builder(
            name = "Stop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(6f, 6f)
                horizontalLineTo(18f)
                verticalLineTo(18f)
                horizontalLineTo(6f)
                close()
            }
        }.build()
    }

    val VolumeOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "VolumeOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(4.34f, 2.93f)
                lineTo(2.93f, 4.34f)
                lineTo(7.29f, 8.7f)
                lineTo(7f, 9f)
                horizontalLineTo(3f)
                verticalLineTo(15f)
                horizontalLineTo(7f)
                lineTo(12f, 20f)
                verticalLineTo(13.41f)
                lineTo(16.45f, 17.86f)
                curveTo(15.71f, 18.42f, 14.9f, 18.84f, 14f, 19.09f)
                verticalLineTo(21.14f)
                curveTo(15.46f, 20.81f, 16.78f, 20.13f, 17.9f, 19.31f)
                lineTo(19.66f, 21.07f)
                lineTo(21.07f, 19.66f)
                lineTo(4.34f, 2.93f)
                close()
                moveTo(12f, 4f)
                lineTo(9.91f, 6.09f)
                lineTo(12f, 8.18f)
                verticalLineTo(4f)
                close()
            }
        }.build()
    }
}

