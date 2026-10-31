package com.unknownrex.sensa.core.capture.parser

import com.unknownrex.sensa.core.domain.capture.NotificationParser

/**
 * One parser per supported payment app. The registry is the only place that knows the full
 * list, so adding an app is a one-line change plus its parser and tests.
 */
class NotificationParserRegistry(
    val parsers: List<NotificationParser> = listOf(
        GoPayParser(),
        BcaMobileParser(),
    ),
) {
    fun parserFor(packageName: String): NotificationParser? =
        parsers.firstOrNull { it.canParse(packageName) }
}
