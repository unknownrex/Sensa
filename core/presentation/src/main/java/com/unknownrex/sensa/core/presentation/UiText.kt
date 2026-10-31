package com.unknownrex.sensa.core.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * A string that either comes from resources or is dynamically produced. Error messages map to
 * [StringResource] so they stay localizable.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        @StringRes val id: Int,
        val args: Array<Any> = emptyArray(),
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            return id == other.id && args.contentEquals(other.args)
        }

        override fun hashCode(): Int = 31 * id + args.contentHashCode()
    }
}

@Composable
fun UiText.asString(): String = asString(LocalContext.current)

/** Non-composable resolver for event handlers and ViewModels that already hold a Context. */
fun UiText.asString(context: android.content.Context): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> context.getString(id, *args)
}
