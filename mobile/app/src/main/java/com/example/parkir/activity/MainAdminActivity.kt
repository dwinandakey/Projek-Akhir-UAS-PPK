package com.example.parkir.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.parkir.R
import com.example.parkir.adapter.LocalDateAdapter
import com.example.parkir.model.ChangePasswordRequest
import com.example.parkir.model.Kendaraan
import com.example.parkir.model.ParkingSummaryResponse
import com.example.parkir.model.PendapatanHarianDto
import com.example.parkir.model.ProfileUpdateRequest
import com.example.parkir.model.UserDto
import com.example.parkir.model.UserStatsResponse
import com.example.parkir.network.ApiClient
import com.example.parkir.network.ApiService
import com.example.parkir.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainAdminActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var incomeReportManager: IncomeReportManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_admin)

        sessionManager = SessionManager(this)
        setupToolbar()
        setupCardClickListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Admin Dashboard"
    }

    private fun setupCardClickListeners() {
        // Location Management
        findViewById<MaterialCardView>(R.id.cardLocations).setOnClickListener {
            startActivity(Intent(this, LocationManagementActivity::class.java))
        }

        // Transaction Management
        findViewById<MaterialCardView>(R.id.cardTransactions).setOnClickListener {
            startActivity(Intent(this, TransactionManagementActivity::class.java))
        }

        // Vehicle Management
        findViewById<MaterialCardView>(R.id.cardVehicles).setOnClickListener {
            startActivity(Intent(this, VehicleManagementActivity::class.java))
        }

        incomeReportManager = IncomeReportManager(
            context = this,
            apiService = ApiClient.apiService,
            token = sessionManager.getAuthToken() ?: ""
        )

        // Reports
        findViewById<MaterialCardView>(R.id.cardReports).setOnClickListener {
            showReportDialog()
        }

        // Search
        findViewById<MaterialCardView>(R.id.cardSearch).setOnClickListener {
            showSearchDialog()
        }
    }

    private fun showReportDialog() {
        val options = arrayOf("Income Report", "Parking Statistics", "User Statistics")
        AlertDialog.Builder(this)
            .setTitle("Select Report Type")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showIncomeReportDialog()
                    1 -> showParkingStatistics()
                    2 -> showUserStatistics()
                }
            }
            .show()
    }

    private fun showSearchDialog() {
        val options = arrayOf("Search Users", "Search Vehicles")
        AlertDialog.Builder(this)
            .setTitle("Select Search Type")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUserSearchDialog()
                    1 -> showVehicleSearchDialog()
                }
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_profile -> {
                showProfileDialog()
                true
            }
            R.id.action_change_password -> {
                showChangePasswordDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        sessionManager.clearSession()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showProfileDialog() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getProfile("Bearer ${sessionManager.getAuthToken()}")
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    showProfileEditDialog(profile)
                } else {
                    showMessage("Gagal mengambil data profil")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            }
        }
    }

    private fun showProfileEditDialog(profile: UserDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)

        etName.setText(profile.name)
        etEmail.setText(profile.email)

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val request = ProfileUpdateRequest(
                    name = etName.text.toString(),
                    email = etEmail.text.toString()
                )
                updateProfile(request)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfile(request: ProfileUpdateRequest) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.updateProfile(
                    "Bearer ${sessionManager.getAuthToken()}",
                    request
                )
                if (response.isSuccessful) {
                    showMessage("Profil berhasil diperbarui")
                } else {
                    showMessage("Gagal memperbarui profil")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Ubah Kata Sandi")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                val oldPassword = etOldPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (oldPassword.isEmpty()) {
                    etOldPassword.error = "Kata sandi lama tidak boleh kosong"
                    return@setPositiveButton
                }
                if (newPassword.isEmpty()) {
                    etNewPassword.error = "Kata sandi baru tidak boleh kosong"
                    return@setPositiveButton
                }
                if (newPassword != confirmPassword) {
                    etConfirmPassword.error = "Konfirmasi kata sandi tidak cocok"
                    return@setPositiveButton
                }

                try {
                    val request = ChangePasswordRequest(
                        currentPassword = oldPassword,
                        newPassword = newPassword
                    )
                    changePassword(request)
                    dialog.dismiss()
                } catch (e: Exception) {
                    showMessage("Error: ${e.message}")
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // MainAdminActivity.kt - Updated changePassword method
    private fun changePassword(request: ChangePasswordRequest) {
        if (request.currentPassword.isEmpty() || request.newPassword.isEmpty()) {
            AlertDialog.Builder(this)
                .setMessage("Data kata sandi tidak valid")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.changePassword(
                    token = "Bearer ${sessionManager.getAuthToken()}",
                    request = request
                )

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful -> {
                            try {
                                // Tangani response body dengan aman
                                val responseBody = response.body()
                                AlertDialog.Builder(this@MainAdminActivity)
                                    .setMessage("Kata sandi berhasil diubah")
                                    .setPositiveButton("OK") { _, _ ->
                                        sessionManager.clearSession()
                                        startActivity(Intent(this@MainAdminActivity, LoginActivity::class.java))
                                        finish()
                                    }
                                    .setCancelable(false)
                                    .show()
                            } catch (e: Exception) {
                                // Jika parsing response body gagal, tetap anggap sukses
                                AlertDialog.Builder(this@MainAdminActivity)
                                    .setMessage("Kata sandi berhasil diubah")
                                    .setPositiveButton("OK") { _, _ ->
                                        sessionManager.clearSession()
                                        startActivity(Intent(this@MainAdminActivity, LoginActivity::class.java))
                                        finish()
                                    }
                                    .setCancelable(false)
                                    .show()
                            }
                        }
                        response.code() == 400 -> {
                            AlertDialog.Builder(this@MainAdminActivity)
                                .setMessage("Kata sandi tidak valid")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        response.code() == 401 -> {
                            AlertDialog.Builder(this@MainAdminActivity)
                                .setMessage("Sesi telah berakhir. Silakan login kembali")
                                .setPositiveButton("OK") { _, _ ->
                                    sessionManager.clearSession()
                                    startActivity(Intent(this@MainAdminActivity, LoginActivity::class.java))
                                    finish()
                                }
                                .setCancelable(false)
                                .show()
                        }
                        else -> {
                            AlertDialog.Builder(this@MainAdminActivity)
                                .setMessage("Gagal mengubah kata sandi")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@MainAdminActivity)
                        .setMessage("Error: ${e.message ?: "Terjadi kesalahan"}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }

    private fun showIncomeReportDialog() {
        incomeReportManager.showDateRangeDialog()
    }

       private fun showParkingStatistics() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getParkingSummary(
                    "Bearer ${sessionManager.getAuthToken()}"
                )
                if (response.isSuccessful && response.body() != null) {
                    showStatisticsDialog("Parking Statistics", response.body()!!)
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            }
        }
    }

    private fun showUserStatistics() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserStats(
                    "Bearer ${sessionManager.getAuthToken()}"
                )
                if (response.isSuccessful && response.body() != null) {
                    showStatisticsDialog("User Statistics", response.body()!!)
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            }
        }
    }

    private fun showUserSearchDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search, null)
        val searchInput = dialogView.findViewById<EditText>(R.id.searchInput)

        AlertDialog.Builder(this)
            .setTitle("Search Users")
            .setView(dialogView)
            .setPositiveButton("Search") { _, _ ->
                val query = searchInput.text.toString()
                if (query.isNotEmpty()) {
                    searchUsers(query)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVehicleSearchDialog() {
        val options = arrayOf("Search by Plate", "Search by Type")
        AlertDialog.Builder(this)
            .setTitle("Select Search Type")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPlateSearchDialog()
                    1 -> showVehicleTypeSearchDialog()
                }
            }
            .show()
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showMessage(message: String) {
        val layout = layoutInflater.inflate(R.layout.custom_toast, null)
        layout.findViewById<TextView>(R.id.toast_text).text = message

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    private class IncomeReportManager(
        private val context: Context,
        private val apiService: ApiService,
        private val token: String
    ) {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val gson = Gson().newBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        fun showDateRangeDialog() {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_date_range, null)
            val startDatePicker = dialogView.findViewById<DatePicker>(R.id.startDatePicker)
            val endDatePicker = dialogView.findViewById<DatePicker>(R.id.endDatePicker)

            // Set initial dates (last 30 days)
            val today = LocalDate.now()
            val thirtyDaysAgo = today.minusDays(30)

            startDatePicker.updateDate(
                thirtyDaysAgo.year,
                thirtyDaysAgo.monthValue - 1,
                thirtyDaysAgo.dayOfMonth
            )

            endDatePicker.updateDate(
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            )

            AlertDialog.Builder(context)
                .setTitle("Pilih Rentang Tanggal")
                .setView(dialogView)
                .setPositiveButton("Tampilkan Laporan") { _, _ ->
                    val startDate = LocalDate.of(
                        startDatePicker.year,
                        startDatePicker.month + 1,
                        startDatePicker.dayOfMonth
                    )
                    val endDate = LocalDate.of(
                        endDatePicker.year,
                        endDatePicker.month + 1,
                        endDatePicker.dayOfMonth
                    )

                    if (endDate.isBefore(startDate)) {
                        showError("Tanggal akhir tidak boleh sebelum tanggal awal")
                        return@setPositiveButton
                    }

                    fetchIncomeReport(startDate, endDate)
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        private fun fetchIncomeReport(startDate: LocalDate, endDate: LocalDate) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val formattedStartDate = startDate.format(dateFormatter)
                    val formattedEndDate = endDate.format(dateFormatter)

                    val response = apiService.getIncomeReport(
                        "Bearer $token",
                        formattedStartDate,
                        formattedEndDate
                    )

                    withContext(Dispatchers.Main) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                response.body()?.let { report ->
                                    showIncomeReportDialog(report)
                                }
                            }
                            response.code() == 401 -> {
                                showError("Sesi telah berakhir. Silakan login kembali.")
                            }
                            else -> {
                                showError("Gagal mengambil laporan: ${response.message()}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showError("Terjadi kesalahan: ${e.message ?: "Unknown error"}")
                    }
                }
            }
        }

        private fun showIncomeReportDialog(report: PendapatanHarianDto) {
            val vehicleStats = report.jumlahPerJenisKendaraan.entries.joinToString("\n") { (type, count) ->
                "• $type: $count kendaraan"
            }

            val message = """
            Periode: ${report.tanggalStart.format(dateFormatter)} sampai ${report.tanggalEnd.format(dateFormatter)}
            
            Total Pendapatan: Rp${formatNumber(report.totalPendapatan)}
            Jumlah Transaksi: ${report.jumlahTransaksi}
            
            Statistik Kendaraan:
            $vehicleStats
        """.trimIndent()

            AlertDialog.Builder(context)
                .setTitle("Laporan Pendapatan")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }

        private fun showError(message: String) {
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }

        private fun formatNumber(number: BigDecimal): String {
            return String.format("%,.0f", number)
        }
    }

    private fun showStatisticsDialog(title: String, data: Any) {
        val message = when (data) {
            is ParkingSummaryResponse -> """
            Total Locations: ${data.totalLokasi}
            Total Capacity: ${data.totalKapasitas}
            Total Occupied: ${data.totalTerisi}
            Total Available: ${data.totalTersedia}
        """.trimIndent()
            is List<*> -> {
                // Updated handling for user statistics
                data.filterIsInstance<UserStatsResponse>()
                    .joinToString("\n") { stat ->
                        "${stat.role}: ${stat.count}"
                    }
            }
            else -> data.toString()
        }


        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun searchUsers(query: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.searchUsers(
                    "Bearer ${sessionManager.getAuthToken()}",
                    query
                )
                if (response.isSuccessful && response.body() != null) {
                    showSearchResults("User Search Results", response.body()!!.toString())
                } else {
                    showMessage("No users found")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            }
        }
    }

    private fun showPlateSearchDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search, null)
        val searchInput = dialogView.findViewById<EditText>(R.id.searchInput)

        AlertDialog.Builder(this)
            .setTitle("Search by Plate Number")
            .setView(dialogView)
            .setPositiveButton("Search") { _, _ ->
                val plateNumber = searchInput.text.toString()
                if (plateNumber.isNotEmpty()) {
                    searchByPlateNumber(plateNumber)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVehicleTypeSearchDialog() {
        val vehicleTypes = arrayOf("MOTOR", "MOBIL")
        AlertDialog.Builder(this)
            .setTitle("Select Vehicle Type")
            .setItems(vehicleTypes) { _, which ->
                searchByVehicleType(vehicleTypes[which])
            }
            .show()
    }

    private fun searchByPlateNumber(plateNumber: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.searchVehicleByPlate(
                    "Bearer ${sessionManager.getAuthToken()}",
                    plateNumber
                )
                if (response.isSuccessful && response.body() != null) {
                    showSearchResults("Vehicle Search Results", response.body()!!.toString())
                } else {
                    showMessage("No vehicles found")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            }
        }
    }

    // Update vehicle search to use enum
    private fun searchByVehicleType(vehicleType: String) {
        try {
            val type = Kendaraan.JenisKendaraan.valueOf(vehicleType)
            lifecycleScope.launch {
                try {
                    val response = ApiClient.apiService.searchVehicleByType(
                        "Bearer ${sessionManager.getAuthToken()}",
                        type.name
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val vehicles = response.body()!!
                        val formattedResults = vehicles.joinToString("\n\n") { vehicle ->
                            """
                        Plate Number: ${vehicle.nomorPlat}
                        Type: ${vehicle.jenisKendaraan.name}
                        ${vehicle.id?.let { "ID: $it" } ?: ""}
                        """.trimIndent()
                        }
                        showSearchResults("Vehicle Search Results", formattedResults)
                    } else {
                        showMessage("No vehicles found")
                    }
                } catch (e: Exception) {
                    showMessage("Error: ${e.message}")
                }
            }
        } catch (e: IllegalArgumentException) {
            showError("Invalid vehicle type: $vehicleType")
        }
    }

    private fun showSearchResults(title: String, results: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(results)
            .setPositiveButton("OK", null)
            .show()
    }
}