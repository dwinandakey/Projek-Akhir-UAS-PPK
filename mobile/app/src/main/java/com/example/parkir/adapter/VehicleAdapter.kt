package com.example.parkir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parkir.databinding.ItemVehicleBinding
import com.example.parkir.model.Vehicle

class VehicleAdapter(
    private val onEdit: (Vehicle) -> Unit,
    private val onDelete: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var vehicles = listOf<Vehicle>()

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount() = vehicles.size

    inner class VehicleViewHolder(
        private val binding: ItemVehicleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle) {
            binding.apply {
                tvPlateNumber.text = vehicle.nomorPlat
                tvVehicleType.text = vehicle.jenisKendaraan

                btnEdit.setOnClickListener { onEdit(vehicle) }
                btnDelete.setOnClickListener { onDelete(vehicle) }
            }
        }
    }
}