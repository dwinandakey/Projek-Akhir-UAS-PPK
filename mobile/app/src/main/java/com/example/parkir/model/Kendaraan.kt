package com.example.parkir.model

data class Kendaraan(
    val id: Long?,
    val nomorPlat: String,
    val jenisKendaraan: JenisKendaraan
) {
    enum class JenisKendaraan {
        MOTOR, MOBIL
    }
}