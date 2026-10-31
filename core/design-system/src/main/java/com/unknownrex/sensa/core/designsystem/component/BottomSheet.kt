package com.unknownrex.sensa.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes

/**
 * Bottom sheet shell: 28 dp top radius, drag handle, 20 dp content padding (design.md §9).
 * Sheets live in their own window, so they use the solid fallback of the glass recipe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensaBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = SensaShapes.bottomSheet,
        containerColor = LocalSensaColors.current.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        content = content,
    )
}
