package com.unknownrex.sensa.core.presentation

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("id", "ID"))

fun Long.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

/** Sticky list header label: Today, Yesterday, or `12 Oct`. */
fun dayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(dayFormatter)
}

fun dayLabel(timestampMillis: Long, today: LocalDate = LocalDate.now()): String =
    dayLabel(timestampMillis.toLocalDate(), today)

/** `Oct 2026` style month label for the dashboard selector. */
fun monthLabel(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale("id", "ID")))
