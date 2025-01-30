// ApiService.kt
package com.example.parkir.network

import com.example.parkir.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body user: UserDto): Response<UserDto>

    @PUT("change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @GET("search/parkir/{keyword}")
    suspend fun searchLokasiParkir(
        @Header("Authorization") token: String,
        @Path("keyword") keyword: String
    ): Response<List<LokasiParkirResponse>>

    @POST("parkir/masuk")
    suspend fun parkirMasuk(
        @Header("Authorization") token: String,
        @Body request: KendaraanMasukRequest
    ): Response<TransaksiParkirResponse>

    @POST("parkir/keluar")
    suspend fun parkirKeluar(
        @Header("Authorization") token: String
    ): Response<TransaksiParkirResponse>

    @GET("parkir/lokasi")
    suspend fun getLokasiParkir(
        @Header("Authorization") token: String
    ): Response<List<LokasiParkirResponse>>

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserDto>

    @PATCH("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<UserDto>

    @GET("search/parkir/summary")
    suspend fun getParkingSummary(
        @Header("Authorization") token: String
    ): Response<ParkingSummaryResponse>

    @GET("search/transaksi/pendapatan/harian/{tanggal_start}/{tanggal_end}")
    suspend fun getIncomeReport(
        @Header("Authorization") token: String,
        @Path("tanggal_start") startDate: String,
        @Path("tanggal_end") endDate: String
    ): Response<PendapatanHarianDto>

    @GET("search/transaksi/{tanggal_start}/{tanggal_end}")
    suspend fun searchTransactions(
        @Header("Authorization") token: String,
        @Path("tanggal_start") startDate: String,
        @Path("tanggal_end") endDate: String
    ): Response<List<TransaksiParkirResponse>>

    @GET("search/kendaraan/plat/{nomorPlat}")
    suspend fun searchVehicleByPlate(
        @Header("Authorization") token: String,
        @Path("nomorPlat") plateNumber: String
    ): Response<Kendaraan>

    @GET("search/kendaraan/jenis/{jenisKendaraan}")
    suspend fun searchVehicleByType(
        @Header("Authorization") token: String,
        @Path("jenisKendaraan") vehicleType: String
    ): Response<List<Kendaraan>>

    @GET("search/users/stats")
    suspend fun getUserStats(
        @Header("Authorization") token: String
    ): Response<List<UserStatsResponse>>

    @GET("search/users/{nameoremail}")
    suspend fun searchUsers(
        @Header("Authorization") token: String,
        @Path("nameoremail") query: String
    ): Response<List<User>>

    @GET("parkir/lokasi")
    suspend fun getLocations(
        @Header("Authorization") token: String
    ): Response<List<ParkingLocation>>

    @POST("parkir/lokasi")
    suspend fun addLocation(
        @Header("Authorization") token: String,
        @Body location: ParkingLocation
    ): Response<ParkingLocation>

    @PUT("parkir/lokasi/{id}")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body location: ParkingLocation
    ): Response<ParkingLocation>

    @DELETE("parkir/lokasi/{id}")
    suspend fun deleteLocation(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("parkir/kendaraan")
    suspend fun getVehicles(
        @Header("Authorization") token: String
    ): Response<List<Vehicle>>

    @GET("parkir/kendaraan/{id}")
    suspend fun getVehicleById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Vehicle>

    @PUT("parkir/kendaraan/{id}")
    suspend fun updateVehicle(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body vehicle: Vehicle
    ): Response<Vehicle>

    @DELETE("parkir/kendaraan/{id}")
    suspend fun deleteVehicle(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @DELETE("parkir/transaksi/{id}")
    suspend fun deleteTransaksiParkir(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    @DELETE("parkir/transaksi/kendaraan/{id}")
    suspend fun deleteTransaksiByKendaraanId(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    @DELETE("parkir/transaksi/profile/{id}")
    suspend fun deleteTransaksiByProfileId(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>
}