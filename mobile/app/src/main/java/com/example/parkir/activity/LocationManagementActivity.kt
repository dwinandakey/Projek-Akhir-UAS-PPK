package com.example.parkir.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.textfield.TextInputEditText
import com.example.parkir.R
import com.example.parkir.adapter.LocationAdapter
import com.example.parkir.databinding.ActivityLocationManagementBinding
import com.example.parkir.model.LocationViewModel
import com.example.parkir.model.LocationViewModelFactory
import com.example.parkir.model.ParkingLocation
import com.example.parkir.utils.SessionManager

class LocationManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationManagementBinding
    private lateinit var adapter: LocationAdapter
    private lateinit var sessionManager: SessionManager
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        binding = ActivityLocationManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = LocationAdapter(
            onEdit = { location -> showEditDialog(location) },
            onDelete = { location -> showDeleteConfirmation(location) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LocationManagementActivity)
            adapter = this@LocationManagementActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.locations.observe(this) { locations ->
            adapter.updateLocations(locations)
            updateSummary(locations)
        }

        viewModel.error.observe(this) { error ->
            MaterialDialog(this).show {
                title(text = "Error")
                message(text = error)
                positiveButton(text = "OK")
            }
        }
    }

    private fun updateSummary(locations: List<ParkingLocation>) {
        val totalLocations = locations.size
        val totalCapacity = locations.sumOf { it.kapasitas }
        binding.tvSummary.text = "Total Locations: $totalLocations\nTotal Capacity: $totalCapacity"
    }

    private fun showAddDialog() {
        MaterialDialog(this).show {
            title(text = "Add New Location")
            customView(R.layout.dialog_location_input)
            positiveButton(text = "Add") {
                val customView = getCustomView()
                val nameInput = customView.findViewById<TextInputEditText>(R.id.etLocationName)
                val capacityInput = customView.findViewById<TextInputEditText>(R.id.etCapacity)

                val name = nameInput.text?.toString()?.trim() ?: ""
                val capacityStr = capacityInput.text?.toString()?.trim() ?: ""

                when {
                    name.isEmpty() -> {
                        nameInput.error = "Location name cannot be empty"
                        return@positiveButton
                    }
                    capacityStr.isEmpty() -> {
                        capacityInput.error = "Capacity cannot be empty"
                        return@positiveButton
                    }
                    capacityStr.toIntOrNull() == null -> {
                        capacityInput.error = "Please enter a valid number"
                        return@positiveButton
                    }
                    capacityStr.toInt() <= 0 -> {
                        capacityInput.error = "Capacity must be greater than 0"
                        return@positiveButton
                    }
                    else -> {
                        viewModel.addLocation(ParkingLocation(
                            namaLokasi = name,
                            kapasitas = capacityStr.toInt()
                        ))
                    }
                }
            }
            negativeButton(text = "Cancel")
        }
    }

    private fun showEditDialog(location: ParkingLocation) {
        MaterialDialog(this).show {
            title(text = "Edit Location")
            customView(R.layout.dialog_location_input)

            // Pre-fill existing values
            getCustomView().apply {
                findViewById<TextInputEditText>(R.id.etLocationName).setText(location.namaLokasi)
                findViewById<TextInputEditText>(R.id.etCapacity).setText(location.kapasitas.toString())
            }

            positiveButton(text = "Save") {
                val customView = getCustomView()
                val nameInput = customView.findViewById<TextInputEditText>(R.id.etLocationName)
                val capacityInput = customView.findViewById<TextInputEditText>(R.id.etCapacity)

                val name = nameInput.text.toString()
                val capacity = capacityInput.text.toString().toIntOrNull() ?: 0

                if (name.isNotBlank() && capacity > 0) {
                    location.id?.let { id ->
                        viewModel.updateLocation(id, ParkingLocation(
                            id = id,
                            namaLokasi = name,
                            kapasitas = capacity
                        ))
                    }
                }
            }
            negativeButton(text = "Cancel")
        }
    }

    private fun showDeleteConfirmation(location: ParkingLocation) {
        MaterialDialog(this).show {
            title(text = "Delete Location")
            message(text = "Are you sure you want to delete ${location.namaLokasi}?")
            positiveButton(text = "Delete") {
                location.id?.let { id -> viewModel.deleteLocation(id) }
            }
            negativeButton(text = "Cancel")
        }
    }
}
