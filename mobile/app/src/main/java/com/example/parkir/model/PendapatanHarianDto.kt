package com.example.parkir.model

import java.math.BigDecimal
import java.time.LocalDate

data class PendapatanHarianDto(
    val tanggalStart: LocalDate,
    val tanggalEnd: LocalDate,
    val totalPendapatan: BigDecimal,
    val jumlahTransaksi: Int,
    val jumlahPerJenisKendaraan: Map<String, Int>
)