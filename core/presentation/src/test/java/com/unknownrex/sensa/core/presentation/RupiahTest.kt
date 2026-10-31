package com.unknownrex.sensa.core.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

class RupiahTest {

    @Test
    fun `formats whole rupiah with id-ID grouping`() {
        assertThat(formatRupiah(0)).isEqualTo("Rp0")
        assertThat(formatRupiah(35_000)).isEqualTo("Rp35.000")
        assertThat(formatRupiah(1_240_000)).isEqualTo("Rp1.240.000")
        assertThat(formatRupiah(1_000_000_000)).isEqualTo("Rp1.000.000.000")
    }

    @Test
    fun `parses typed amounts`() {
        assertThat(parseRupiahInput("35.000")).isEqualTo(35_000)
        assertThat(parseRupiahInput("35000")).isEqualTo(35_000)
        assertThat(parseRupiahInput("Rp 35.000")).isEqualTo(35_000)
    }

    @Test
    fun `rejects empty and zero amounts`() {
        assertThat(parseRupiahInput("")).isNull()
        assertThat(parseRupiahInput("abc")).isNull()
        assertThat(parseRupiahInput("0")).isNull()
        assertThat(parseRupiahInput("000")).isNull()
    }

    @Test
    fun `groups digits while typing`() {
        assertThat(formatRupiahDigits("35000")).isEqualTo("35.000")
        assertThat(formatRupiahDigits("Rp 1.240.000")).isEqualTo("1.240.000")
        assertThat(formatRupiahDigits("")).isEqualTo("")
        assertThat(formatRupiahDigits("0000")).isEqualTo("")
    }
}
