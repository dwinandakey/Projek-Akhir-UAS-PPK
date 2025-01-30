package com.example.parkir.model

data class TransaksiParkirResponse(
    val id: Long,
    val nomorPlat: String,
    val jenisKendaraan: String,
    val kendaraan: Kendaraan,
    val waktuMasuk: String,
    val waktuKeluar: String?,
    val biaya: Double,
    val status: String,
    val lokasiParkir: LokasiParkir // Changed from String to LokasiParkir object
)

data class LokasiParkir(
    val id: Long,
    val namaLokasi: String,
    val kapasitas: Int,
    val terisi: Int,
    val status: Boolean
)
