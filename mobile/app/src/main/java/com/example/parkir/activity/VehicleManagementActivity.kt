package com.example.parkir.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.example.parkir.R
import com.example.parkir.adapter.VehicleAdapter
import com.example.parkir.databinding.ActivityVehicleManagementBinding
import com.example.parkir.model.Vehicle
import com.example.parkir.model.VehicleViewModel
import com.example.parkir.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import androidx.core.view.isVisible
import com.example.parkir.model.VehicleViewModelFactory

class VehicleManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVehicleManagementBinding
    private lateinit var adapter: VehicleAdapter
    private lateinit var sessionManager: SessionManager
    private val viewModel: VehicleViewModel by viewModels {
        VehicleViewModelFactory(sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        binding = ActivityVehicleManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = VehicleAdapter(
            onEdit = { vehicle -> showEditDialog(vehicle) },
            onDelete = { vehicle -> showDeleteConfirmation(vehicle) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@VehicleManagementActivity)
            adapter = this@VehicleManagementActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.vehicles.observe(this) { vehicles ->
            adapter.updateVehicles(vehicles)
        }

        viewModel.error.observe(this) { error ->
            MaterialDialog(this).show {
                title(text = "Error")
                message(text = error)
                positiveButton(text = "OK")
            }
        }
    }

    private fun showEditDialog(vehicle: Vehicle) {
        MaterialDialog(this).show {
            title(text = "Edit Vehicle")
            customView(R.layout.dialog_vehicle_input)

            getCustomView().apply {
                val etPlateNumber = findViewById<TextInputEditText>(R.id.etPlateNumber)
                val spinnerVehicleType = findViewById<Spinner>(R.id.spinnerVehicleType)

                etPlateNumber.setText(vehicle.nomorPlat)

                // Data untuk spinner
                val vehicleTypes = listOf("MOBIL", "MOTOR")
                val adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    vehicleTypes
                )
                spinnerVehicleType.adapter = adapter

                // Pilih item sesuai data kendaraan
                val selectedIndex = vehicleTypes.indexOf(vehicle.jenisKendaraan)
                if (selectedIndex >= 0) {
                    spinnerVehicleType.setSelection(selectedIndex)
                }
            }

            positiveButton(text = "Save") {
                val customView = getCustomView()
                val plateNumber = customView.findViewById<TextInputEditText>(R.id.etPlateNumber)
                    .text.toString().trim()
                val vehicleType = customView.findViewById<Spinner>(R.id.spinnerVehicleType)
                    .selectedItem.toString()

                if (plateNumber.isNotEmpty()) {
                    vehicle.id?.let { id ->
                        viewModel.updateVehicle(
                            id,
                            Vehicle(id = id, nomorPlat = plateNumber, jenisKendaraan = vehicleType)
                        )
                    }
                }
            }
            negativeButton(text = "Cancel")
        }
    }

    private fun showDeleteConfirmation(vehicle: Vehicle) {
        MaterialDialog(this).show {
            title(text = "Delete Vehicle")
            message(text = "Are you sure you want to delete vehicle ${vehicle.nomorPlat}?")
            positiveButton(text = "Delete") {
                vehicle.id?.let { id -> viewModel.deleteVehicle(id) }
            }
            negativeButton(text = "Cancel")
        }
    }
}