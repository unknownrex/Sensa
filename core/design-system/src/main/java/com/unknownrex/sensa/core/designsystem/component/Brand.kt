package com.unknownrex.sensa.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unknownrex.sensa.core.designsystem.R

/**
 * The Sensa "S" mark. The brand vector draws the S in near-white, which disappears on the light
 * theme, so light surfaces use a dark-S variant. Tinting is not an option: it would flatten the
 * mint dot.
 */
@Composable
fun SensaMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    contentDescription: String? = "Sensa",
) {
    Image(
        painter = painterResource(
            if (isSystemInDarkTheme()) R.drawable.sensa_mark else R.drawable.sensa_mark_light,
        ),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
    )
}
