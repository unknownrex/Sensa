package com.unknownrex.sensa.core.domain

sealed interface DataError : Error {
    enum class Local : DataError {
        NOT_FOUND,
        DUPLICATE,
        DISK_FULL,
        UNKNOWN,
    }

    /**
     * Kept for later phases (export/backup, AI suggestions). Core flows are strictly on-device,
     * so nothing in the MVP maps to these yet.
     */
    enum class Network : DataError {
        NO_INTERNET,
        SERVER_ERROR,
        UNKNOWN,
    }
}
