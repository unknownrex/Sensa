package com.unknownrex.sensa.core.domain

import java.util.UUID

/** Stable, opaque ids for rows created on device. */
fun newId(): String = UUID.randomUUID().toString()
