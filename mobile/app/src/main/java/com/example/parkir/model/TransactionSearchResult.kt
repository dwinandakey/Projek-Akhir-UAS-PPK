package com.example.parkir.model

data class TransactionSearchResult(
    val id: Long,
    val nomorPlat: String,
    val jenisKendaraan: Kendaraan.JenisKendaraan,
    val waktuMasuk: String,
    val waktuKeluar: String?,
    val biaya: Long?,
    val lokasiParkir: LokasiParkirResponse?,
    val status: String
)