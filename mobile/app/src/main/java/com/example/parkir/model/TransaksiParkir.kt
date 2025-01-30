package com.example.parkir.model

data class TransaksiParkir(
    val id: Long,
    val nomorPlat: String,
    val jenisKendaraan: String,
    val waktuMasuk: String,
    val waktuKeluar: String?,
    val biaya: Double,
    val status: String,
    val lokasiParkir: String
)