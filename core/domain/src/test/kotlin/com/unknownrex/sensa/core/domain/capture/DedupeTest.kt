package com.unknownrex.sensa.core.domain.capture

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import com.unknownrex.sensa.core.domain.model.PaymentSource
import org.junit.jupiter.api.Test

class DedupeTest {

    @Test
    fun `normalizeMerchant lowercases and collapses punctuation`() {
        assertThat(normalizeMerchant("Kopi Kenangan!!")).isEqualTo("kopi kenangan")
        assertThat(normalizeMerchant("  WARUNG   Bu Siti ")).isEqualTo("warung bu siti")
        assertThat(normalizeMerchant("Toko-Maju_99")).isEqualTo("toko maju 99")
    }

    @Test
    fun `same payment inside the window shares a key`() {
        val first = dedupeKey(input(amount = 25_000, merchant = "Kopi Kenangan", at = 1_000_000))
        val second = dedupeKey(input(amount = 25_000, merchant = "KOPI KENANGAN", at = 1_010_000))

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `different amount, merchant, source or window produces a different key`() {
        val base = dedupeKey(input(amount = 25_000, merchant = "Kopi Kenangan", at = 1_000_000))

        assertThat(dedupeKey(input(amount = 26_000, merchant = "Kopi Kenangan", at = 1_000_000)))
            .isNotEqualTo(base)
        assertThat(dedupeKey(input(amount = 25_000, merchant = "Warung Bu Siti", at = 1_000_000)))
            .isNotEqualTo(base)
        assertThat(
            dedupeKey(input(amount = 25_000, merchant = "Kopi Kenangan", at = 1_000_000, source = PaymentSource.BCA_MOBILE)),
        ).isNotEqualTo(base)
        assertThat(
            dedupeKey(
                input(
                    amount = 25_000,
                    merchant = "Kopi Kenangan",
                    at = 1_000_000 + DEDUPE_WINDOW_MILLIS,
                ),
            ),
        ).isNotEqualTo(base)
    }

    private fun input(
        amount: Long,
        merchant: String,
        at: Long,
        source: PaymentSource = PaymentSource.GOPAY,
    ) = DedupeInput(
        source = source,
        amountRupiah = amount,
        merchant = merchant,
        timestampMillis = at,
    )
}
