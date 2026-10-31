package com.unknownrex.sensa.core.presentation

import com.unknownrex.sensa.core.domain.DataError

fun DataError.Local.toUiText(): UiText = when (this) {
    DataError.Local.NOT_FOUND -> UiText.DynamicString("That transaction could not be found.")
    DataError.Local.DUPLICATE -> UiText.DynamicString("This transaction was already saved.")
    DataError.Local.DISK_FULL -> UiText.DynamicString("Storage is full. Free up space and try again.")
    DataError.Local.UNKNOWN -> UiText.DynamicString("Something went wrong. Please try again.")
}
