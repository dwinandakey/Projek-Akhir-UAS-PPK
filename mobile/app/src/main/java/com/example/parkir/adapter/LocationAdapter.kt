package com.example.parkir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parkir.databinding.ItemLocationBinding
import com.example.parkir.model.ParkingLocation

class LocationAdapter(
    private val onEdit: (ParkingLocation) -> Unit,
    private val onDelete: (ParkingLocation) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    private var locations = listOf<ParkingLocation>()

    fun updateLocations(newLocations: List<ParkingLocation>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount() = locations.size

    inner class LocationViewHolder(
        private val binding: ItemLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: ParkingLocation) {
            binding.apply {
                tvLocationName.text = location.namaLokasi
                tvCapacity.text = "Capacity: ${location.kapasitas}"

                btnEdit.setOnClickListener { onEdit(location) }
                btnDelete.setOnClickListener { onDelete(location) }
            }
        }
    }
}