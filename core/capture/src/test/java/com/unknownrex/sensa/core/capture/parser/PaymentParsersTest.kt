package com.unknownrex.sensa.core.capture.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.unknownrex.sensa.core.capture.PaymentApps
import com.unknownrex.sensa.core.domain.capture.NotificationSnapshot
import com.unknownrex.sensa.core.domain.model.PaymentSource
import org.junit.jupiter.api.Test

/**
 * PROVISIONAL SAMPLES.
 *
 * These strings mirror the notification shapes the parsers are written against. They must be
 * replaced with real redacted notifications from `.agents/samples/gopay/` and
 * `.agents/samples/bca/` before this feature can be trusted.
 */
class PaymentParsersTest {

    private val goPay = GoPayParser()
    private val bca = BcaMobileParser()

    @Test
    fun `GoPay payment extracts amount and merchant`() {
        val parsed = goPay.parse(gopay("Pembayaran berhasil Rp25.000 ke Kopi Kenangan"))

        assertThat(parsed).isNotNull()
        assertThat(parsed!!.amountRupiah).isEqualTo(25_000L)
        assertThat(parsed.merchant).isEqualTo("Kopi Kenangan")
        assertThat(parsed.source).isEqualTo(PaymentSource.GOPAY)
        assertThat(parsed.isTransfer).isEqualTo(false)
    }

    @Test
    fun `GoPay QRIS ignores trailing app hint`() {
        val parsed = goPay.parse(
            gopay("Pembayaran berhasil! Rp25.000 ke Kopi Kenangan. Lihat detail di aplikasi GoPay."),
        )

        assertThat(parsed).isNotNull()
        assertThat(parsed!!.merchant).isEqualTo("Kopi Kenangan")
    }

    @Test
    fun `GoPay transfer is flagged as transfer`() {
        val parsed = goPay.parse(gopay("Kamu berhasil transfer Rp100.000 ke BUDI.", title = "GoPay"))

        assertThat(parsed).isNotNull()
        assertThat(parsed!!.amountRupiah).isEqualTo(100_000L)
        assertThat(parsed.merchant).isEqualTo("BUDI")
        assertThat(parsed.isTransfer).isTrue()
    }

    @Test
    fun `GoPay promos are not payments`() {
        val parsed = goPay.parse(gopay("Promo seru minggu ini! Diskon 50% untuk semua merchant."))

        assertThat(parsed).isEqualTo(null)
    }

    @Test
    fun `GoPay parser ignores other packages`() {
        val parsed = goPay.parse(
            NotificationSnapshot(
                packageName = PaymentApps.BCA_MOBILE,
                title = "BCA mobile",
                text = "Pembayaran berhasil Rp25.000 ke Kopi Kenangan",
                bigText = null,
                postedAtMillis = 1_000_000L,
            ),
        )

        assertThat(parsed).isEqualTo(null)
    }

    @Test
    fun `BCA QRIS payment extracts amount and merchant`() {
        val parsed = bca.parse(
            bca("Transaksi Berhasil. Pembayaran QRIS sebesar Rp35.000 di WARUNG MAKAN SEDERHANA."),
        )

        assertThat(parsed).isNotNull()
        assertThat(parsed!!.amountRupiah).isEqualTo(35_000L)
        assertThat(parsed.merchant).isEqualTo("WARUNG MAKAN SEDERHANA")
        assertThat(parsed.source).isEqualTo(PaymentSource.BCA_MOBILE)
    }

    @Test
    fun `BCA transfer is flagged and routed to review`() {
        val parsed = bca.parse(bca("Transaksi Berhasil. Transfer ke BUDI sebesar Rp50.000"))

        assertThat(parsed).isNotNull()
        assertThat(parsed!!.amountRupiah).isEqualTo(50_000L)
        assertThat(parsed.merchant).isEqualTo("BUDI")
        assertThat(parsed.isTransfer).isTrue()
    }

    @Test
    fun `BCA marketing notification is not a payment`() {
        val parsed = bca.parse(bca("Nikmati promo cashback hingga 10% di merchant favoritmu."))

        assertThat(parsed).isEqualTo(null)
    }

    private fun gopay(text: String, title: String = "GoPay") = NotificationSnapshot(
        packageName = PaymentApps.GOPAY,
        title = title,
        text = text,
        bigText = null,
        postedAtMillis = 1_000_000L,
    )

    private fun bca(text: String) = NotificationSnapshot(
        packageName = PaymentApps.BCA_MOBILE,
        title = "BCA mobile",
        text = text,
        bigText = null,
        postedAtMillis = 1_000_000L,
    )
}
